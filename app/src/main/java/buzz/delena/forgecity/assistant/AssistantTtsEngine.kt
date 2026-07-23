package buzz.delena.forgecity.assistant

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.os.Build
import android.speech.tts.TextToSpeech
import buzz.delena.forgecity.house.AssistantHouseBridge
import buzz.delena.forgecity.house.AssistantPresenceState
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Locale
import java.util.concurrent.Executors

class AssistantTtsEngine(context: Context) {
    private val appContext = context.applicationContext
    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var tts: TextToSpeech? = null
    private var readiness: Readiness = Readiness.INITIALIZING
    private val readyCallbacks = mutableListOf<(Readiness) -> Unit>()
    private var focusRequest: AudioFocusRequest? = null
    private var pcmTrack: AudioTrack? = null
    private var mediaPlayer: MediaPlayer? = null
    private val pcmExecutor = Executors.newSingleThreadExecutor { task ->
        Thread(task, "forgecity-pcm-play").apply { isDaemon = true }
    }

    enum class Readiness {
        INITIALIZING,
        READY,
        UNAVAILABLE,
    }

    enum class SpeakResult {
        STARTED,
        EMPTY,
        UNAVAILABLE,
    }

    init {
        ensureReady()
    }

    @Synchronized
    fun ensureReady(callback: ((Readiness) -> Unit)? = null) {
        if (readiness != Readiness.INITIALIZING) {
            callback?.invoke(readiness)
            return
        }
        callback?.let { readyCallbacks += it }
        if (tts != null) return
        tts = TextToSpeech(appContext) { status ->
            val engine = tts
            val result = if (status == TextToSpeech.SUCCESS && engine != null) {
                Readiness.READY
            } else {
                Readiness.UNAVAILABLE
            }
            completeReadiness(result)
        }
    }

    fun speakDirect(text: String, callback: ((SpeakResult) -> Unit)? = null) {
        speak(text, listOf(Locale.getDefault()), 1.0f, callback)
    }

    fun speakTamil(text: String, callback: ((SpeakResult) -> Unit)? = null) {
        speak(
            text = text,
            locales = listOf(Locale.forLanguageTag("ta-IN"), Locale("ta")),
            speechRate = 0.9f,
            callback = callback,
        )
    }

    /**
     * Play Gemini native audio. Accepts raw L16 PCM or a RIFF/WAV wrapper.
     * Uses streamed AudioTrack first; falls back to MediaPlayer + temp WAV.
     */
    fun playPcm(
        pcm: ByteArray,
        sampleRateHz: Int,
        callback: ((SpeakResult) -> Unit)? = null,
    ) {
        if (pcm.isEmpty()) {
            ForgeCityTtsDiagnostics.warn("pcm_blocked", "reason=empty")
            callback?.invoke(SpeakResult.EMPTY)
            return
        }
        pcmExecutor.execute {
            var reported = false
            try {
                stopPlaybackLocked()
                tts?.stop()
                val normalized = PcmAudioNormalizer.normalize(pcm, sampleRateHz)
                if (normalized.pcm.isEmpty()) {
                    ForgeCityTtsDiagnostics.warn("pcm_blocked", "reason=empty_after_normalize")
                    callback?.invoke(SpeakResult.EMPTY)
                    return@execute
                }
                ForgeCityTtsDiagnostics.info(
                    "pcm_play_attempt",
                    "bytes=${normalized.pcm.size} rateHz=${normalized.sampleRateHz} wav=${normalized.hadWavHeader}",
                )
                requestFocus()
                val speakMs = pcmDurationMs(normalized.pcm.size, normalized.sampleRateHz)
                if (playViaAudioTrack(normalized.pcm, normalized.sampleRateHz)) {
                    reported = true
                    notifySpeakStarted(callback, speakMs)
                    waitForPcmDuration(normalized.pcm.size, normalized.sampleRateHz)
                    return@execute
                }
                ForgeCityTtsDiagnostics.warn("pcm_audiotrack_failed", "trying=mediaplayer")
                if (playViaMediaPlayer(normalized.pcm, normalized.sampleRateHz)) {
                    reported = true
                    notifySpeakStarted(callback, speakMs)
                    waitForPcmDuration(normalized.pcm.size, normalized.sampleRateHz)
                    return@execute
                }
                ForgeCityTtsDiagnostics.warn("pcm_play_failed", "reason=all_backends")
                callback?.invoke(SpeakResult.UNAVAILABLE)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            } catch (e: Exception) {
                ForgeCityTtsDiagnostics.warn(
                    "pcm_play_failed",
                    "reason=exception type=${e.javaClass.simpleName}",
                )
                if (!reported) callback?.invoke(SpeakResult.UNAVAILABLE)
            } finally {
                stopPlaybackLocked()
                abandonFocus()
                AssistantHouseBridge.clear()
            }
        }
    }

    private fun playViaAudioTrack(pcm: ByteArray, sampleRateHz: Int): Boolean {
        val minBuf = AudioTrack.getMinBufferSize(
            sampleRateHz,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        if (minBuf <= 0) {
            ForgeCityTtsDiagnostics.warn("pcm_blocked", "reason=bad_buffer minBuf=$minBuf")
            return false
        }
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()
        val format = AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(sampleRateHz)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()
        val bufferSize = (minBuf * 4).coerceAtLeast(pcm.size.coerceAtMost(minBuf * 8))
        val track = try {
            AudioTrack.Builder()
                .setAudioAttributes(attrs)
                .setAudioFormat(format)
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
                .build()
        } catch (e: Exception) {
            // PERFORMANCE_MODE_LOW_LATENCY not available on all devices — retry plain.
            try {
                AudioTrack.Builder()
                    .setAudioAttributes(attrs)
                    .setAudioFormat(format)
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            } catch (e2: Exception) {
                ForgeCityTtsDiagnostics.warn(
                    "pcm_blocked",
                    "reason=track_build type=${e2.javaClass.simpleName}",
                )
                return false
            }
        }
        if (track.state != AudioTrack.STATE_INITIALIZED) {
            track.release()
            ForgeCityTtsDiagnostics.warn("pcm_blocked", "reason=track_init state=${track.state}")
            return false
        }
        synchronized(this) { pcmTrack = track }
        return try {
            // Prime a first chunk before play() — more reliable on some ColorOS builds.
            val firstChunk = minOf(minBuf, pcm.size)
            var written = track.write(pcm, 0, firstChunk)
            if (written < 0) {
                ForgeCityTtsDiagnostics.warn("pcm_write_failed", "written=$written offset=0")
                return false
            }
            track.play()
            var offset = written.coerceAtLeast(0)
            var stallLoops = 0
            while (offset < pcm.size) {
                val chunk = minOf(minBuf, pcm.size - offset)
                written = track.write(pcm, offset, chunk)
                if (written < 0) {
                    ForgeCityTtsDiagnostics.warn("pcm_write_failed", "written=$written offset=$offset")
                    return false
                }
                if (written == 0) {
                    stallLoops++
                    if (stallLoops > 200) {
                        ForgeCityTtsDiagnostics.warn("pcm_write_failed", "reason=stall offset=$offset")
                        return false
                    }
                    Thread.sleep(10)
                    continue
                }
                stallLoops = 0
                offset += written
            }
            ForgeCityTtsDiagnostics.info(
                "pcm_play_started",
                "backend=audiotrack bytes=${pcm.size} rateHz=$sampleRateHz",
            )
            true
        } catch (e: Exception) {
            ForgeCityTtsDiagnostics.warn(
                "pcm_audiotrack_exception",
                "type=${e.javaClass.simpleName}",
            )
            false
        }
    }

    private fun playViaMediaPlayer(pcm: ByteArray, sampleRateHz: Int): Boolean {
        val wavFile = File(appContext.cacheDir, "forgecity-gemini-tts.wav")
        return try {
            wavFile.writeBytes(PcmAudioNormalizer.toWav(pcm, sampleRateHz))
            val player = MediaPlayer()
            synchronized(this) { mediaPlayer = player }
            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build(),
            )
            player.setDataSource(wavFile.absolutePath)
            player.prepare()
            player.start()
            ForgeCityTtsDiagnostics.info(
                "pcm_play_started",
                "backend=mediaplayer bytes=${pcm.size} rateHz=$sampleRateHz",
            )
            true
        } catch (e: Exception) {
            ForgeCityTtsDiagnostics.warn(
                "pcm_mediaplayer_failed",
                "type=${e.javaClass.simpleName}",
            )
            false
        }
    }

    private fun pcmDurationMs(byteCount: Int, sampleRateHz: Int): Long =
        ((byteCount / 2.0) / sampleRateHz * 1000.0).toLong().coerceAtLeast(50L) + 120L

    private fun waitForPcmDuration(byteCount: Int, sampleRateHz: Int) {
        try {
            Thread.sleep(pcmDurationMs(byteCount, sampleRateHz))
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

    private fun speak(
        text: String,
        locales: List<Locale>,
        speechRate: Float,
        callback: ((SpeakResult) -> Unit)?,
    ) {
        val line = text.trim()
        if (line.isEmpty()) {
            ForgeCityTtsDiagnostics.warn("tts_blocked", "reason=empty")
            callback?.invoke(SpeakResult.EMPTY)
            return
        }
        ensureReady { state ->
            if (state != Readiness.READY) {
                ForgeCityTtsDiagnostics.warn("tts_blocked", "reason=engine_$state")
                callback?.invoke(SpeakResult.UNAVAILABLE)
                return@ensureReady
            }
            val result = speakWithLocale(line, locales, speechRate)
            ForgeCityTtsDiagnostics.info(
                "tts_speak_result",
                "localeCandidates=${locales.joinToString { it.toLanguageTag() }} result=$result",
            )
            if (result == SpeakResult.STARTED) {
                notifySpeakStarted(callback)
            } else {
                callback?.invoke(result)
            }
        }
    }

    /** House pulse only — never forwards utterance / notification text. */
    private fun notifySpeakStarted(
        callback: ((SpeakResult) -> Unit)?,
        durationMs: Long = AssistantPresenceState.DEFAULT_SPEAK_MS,
    ) {
        AssistantHouseBridge.onSpeakStarted(durationMs)
        callback?.invoke(SpeakResult.STARTED)
    }

    @Synchronized
    private fun speakWithLocale(
        line: String,
        locales: List<Locale>,
        speechRate: Float,
    ): SpeakResult {
        val engine = tts ?: return SpeakResult.UNAVAILABLE
        val localeSelected = locales.any { locale ->
            engine.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE &&
                engine.setLanguage(locale) >= TextToSpeech.LANG_AVAILABLE
        }
        if (!localeSelected) return SpeakResult.UNAVAILABLE
        engine.setSpeechRate(speechRate)
        requestFocus()
        val result = engine.speak(
            line,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "forgecity-${System.nanoTime()}",
        )
        return if (result == TextToSpeech.SUCCESS) {
            SpeakResult.STARTED
        } else {
            SpeakResult.UNAVAILABLE
        }
    }

    fun stop() {
        tts?.stop()
        stopPlaybackLocked()
        abandonFocus()
        AssistantHouseBridge.clear()
    }

    @Synchronized
    private fun stopPlaybackLocked() {
        runCatching {
            pcmTrack?.pause()
            pcmTrack?.flush()
            pcmTrack?.stop()
            pcmTrack?.release()
        }
        pcmTrack = null
        runCatching {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        }
        mediaPlayer = null
    }

    fun shutdown() {
        stop()
        pcmExecutor.shutdownNow()
        tts?.shutdown()
        tts = null
        synchronized(this) {
            readiness = Readiness.UNAVAILABLE
            readyCallbacks.clear()
        }
    }

    @Synchronized
    private fun completeReadiness(result: Readiness) {
        readiness = result
        val callbacks = readyCallbacks.toList()
        readyCallbacks.clear()
        callbacks.forEach { it(result) }
    }

    private fun requestFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build(),
                )
                .build()
            focusRequest = req
            audioManager.requestAudioFocus(req)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK,
            )
        }
    }

    private fun abandonFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
            focusRequest = null
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
    }
}

/** Normalize Gemini audio bytes (raw L16 or RIFF/WAV) for playback. */
internal object PcmAudioNormalizer {
    data class NormalizedPcm(
        val pcm: ByteArray,
        val sampleRateHz: Int,
        val hadWavHeader: Boolean,
    )

    fun normalize(raw: ByteArray, fallbackRateHz: Int): NormalizedPcm {
        val rate = fallbackRateHz.coerceIn(8_000, 48_000)
        if (raw.size >= 12 &&
            raw[0] == 'R'.code.toByte() &&
            raw[1] == 'I'.code.toByte() &&
            raw[2] == 'F'.code.toByte() &&
            raw[3] == 'F'.code.toByte()
        ) {
            val parsed = parseWav(raw)
            if (parsed != null) return parsed
        }
        val even = if (raw.size % 2 == 0) raw else raw.copyOf(raw.size - 1)
        return NormalizedPcm(pcm = even, sampleRateHz = rate, hadWavHeader = false)
    }

    fun toWav(pcm: ByteArray, sampleRateHz: Int): ByteArray {
        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
        val dataSize = pcm.size
        header.put("RIFF".toByteArray(Charsets.US_ASCII))
        header.putInt(36 + dataSize)
        header.put("WAVE".toByteArray(Charsets.US_ASCII))
        header.put("fmt ".toByteArray(Charsets.US_ASCII))
        header.putInt(16)
        header.putShort(1) // PCM
        header.putShort(1) // mono
        header.putInt(sampleRateHz)
        header.putInt(sampleRateHz * 2) // byte rate
        header.putShort(2) // block align
        header.putShort(16) // bits
        header.put("data".toByteArray(Charsets.US_ASCII))
        header.putInt(dataSize)
        return header.array() + pcm
    }

    private fun parseWav(bytes: ByteArray): NormalizedPcm? {
        if (bytes.size < 44) return null
        var offset = 12
        var sampleRate = 24_000
        var data: ByteArray? = null
        while (offset + 8 <= bytes.size) {
            val id = String(bytes, offset, 4, Charsets.US_ASCII)
            val size = ByteBuffer.wrap(bytes, offset + 4, 4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .int
                .coerceAtLeast(0)
            val chunkStart = offset + 8
            val chunkEnd = (chunkStart + size).coerceAtMost(bytes.size)
            when (id) {
                "fmt " -> {
                    if (size >= 16 && chunkStart + 16 <= bytes.size) {
                        val fmt = ByteBuffer.wrap(bytes, chunkStart, 16).order(ByteOrder.LITTLE_ENDIAN)
                        val audioFormat = fmt.short.toInt() and 0xffff
                        val channels = fmt.short.toInt() and 0xffff
                        sampleRate = fmt.int.coerceIn(8_000, 48_000)
                        if (audioFormat != 1 || channels != 1) return null
                    }
                }
                "data" -> {
                    data = bytes.copyOfRange(chunkStart, chunkEnd)
                }
            }
            offset = chunkEnd + (if (size % 2 == 1) 1 else 0)
            if (id == "data") break
        }
        val pcm = data ?: return null
        val even = if (pcm.size % 2 == 0) pcm else pcm.copyOf(pcm.size - 1)
        return NormalizedPcm(pcm = even, sampleRateHz = sampleRate, hadWavHeader = true)
    }
}
