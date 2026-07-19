package buzz.delena.forgecity.assistant

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.speech.tts.TextToSpeech
import java.util.Locale

class AssistantTtsEngine(context: Context) {
    private val appContext = context.applicationContext
    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var tts: TextToSpeech? = null
    private var readiness: Readiness = Readiness.INITIALIZING
    private val readyCallbacks = mutableListOf<(Readiness) -> Unit>()
    private var focusRequest: AudioFocusRequest? = null

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

    private fun speak(
        text: String,
        locales: List<Locale>,
        speechRate: Float,
        callback: ((SpeakResult) -> Unit)?,
    ) {
        val line = text.trim()
        if (line.isEmpty()) {
            callback?.invoke(SpeakResult.EMPTY)
            return
        }
        ensureReady { state ->
            if (state != Readiness.READY) {
                callback?.invoke(SpeakResult.UNAVAILABLE)
                return@ensureReady
            }
            callback?.invoke(speakWithLocale(line, locales, speechRate))
        }
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
        abandonFocus()
    }

    fun shutdown() {
        stop()
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
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
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
