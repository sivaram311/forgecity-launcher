package buzz.delena.forgecity.assistant.gemini

import buzz.delena.forgecity.assistant.ForgeCityTtsDiagnostics
import java.io.ByteArrayOutputStream
import java.io.InterruptedIOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder
import java.util.Base64
import javax.net.ssl.HttpsURLConnection

/**
 * Gemini native audio TTS via `generateContent` with `responseModalities: ["AUDIO"]`.
 * Returns raw PCM (typically audio/L16 @ 24 kHz); does not speak — caller plays bytes.
 */
class GeminiAudioTtsClient(
    private val connectTimeoutMs: Int = 8_000,
    private val readTimeoutMs: Int = 45_000,
    private val maxResponseBytes: Int = 3 * 1024 * 1024,
) : AutoCloseable {
    @Volatile
    private var closed = false

    fun synthesize(
        apiKey: String,
        model: String,
        prompt: String,
        voice: String = DEFAULT_VOICE,
        languageCode: String = DEFAULT_LANGUAGE,
    ): GeminiAudioResult {
        if (closed || apiKey.isBlank() || prompt.isBlank()) {
            ForgeCityTtsDiagnostics.warn("gemini_audio_blocked", "reason=closed_or_blank")
            return GeminiAudioResult.Unavailable
        }
        val modelId = normalizeTtsModel(model)
        val voiceName = voice.trim().ifBlank { DEFAULT_VOICE }
        val lang = languageCode.trim().ifBlank { DEFAULT_LANGUAGE }
        val url = validatedUrl(modelId) ?: return GeminiAudioResult.Unavailable
        val startedAt = System.currentTimeMillis()
        ForgeCityTtsDiagnostics.info(
            "gemini_audio_start",
            "model=$modelId voice=$voiceName lang=$lang",
        )
        var connection: HttpsURLConnection? = null
        return try {
            connection = (url.openConnection() as? HttpsURLConnection)
                ?: return GeminiAudioResult.Unavailable
            connection.requestMethod = "POST"
            connection.connectTimeout = connectTimeoutMs
            connection.readTimeout = readTimeoutMs
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.setRequestProperty("x-goog-api-key", apiKey.trim())
            val payload = buildRequestBody(prompt, voiceName, lang).toByteArray(Charsets.UTF_8)
            connection.setFixedLengthStreamingMode(payload.size)
            connection.outputStream.use { it.write(payload) }
            val status = connection.responseCode
            ForgeCityTtsDiagnostics.info(
                "gemini_audio_http",
                "status=$status model=$modelId elapsedMs=${System.currentTimeMillis() - startedAt}",
            )
            when (status) {
                HttpURLConnection.HTTP_OK -> {
                    val body = readBounded(connection.inputStream)
                        ?: return GeminiAudioResult.Malformed
                    parseSuccess(body.toString(Charsets.UTF_8))
                }
                HttpURLConnection.HTTP_UNAUTHORIZED,
                HttpURLConnection.HTTP_FORBIDDEN,
                -> GeminiAudioResult.Unauthorized
                HttpURLConnection.HTTP_BAD_REQUEST -> {
                    val err = readBounded(connection.errorStream)?.toString(Charsets.UTF_8).orEmpty()
                    when {
                        err.contains("API_KEY_INVALID", ignoreCase = true) ||
                            err.contains("API key not valid", ignoreCase = true) ->
                            GeminiAudioResult.Unauthorized
                        err.contains("not found", ignoreCase = true) ||
                            err.contains("NOT_FOUND", ignoreCase = true) ->
                            GeminiAudioResult.ModelUnavailable
                        else -> {
                            ForgeCityTtsDiagnostics.warn("gemini_audio_bad_request", "status=400")
                            GeminiAudioResult.Unavailable
                        }
                    }
                }
                HttpURLConnection.HTTP_NOT_FOUND -> GeminiAudioResult.ModelUnavailable
                HttpURLConnection.HTTP_GATEWAY_TIMEOUT,
                429,
                -> GeminiAudioResult.Timeout
                else -> GeminiAudioResult.Unavailable
            }
        } catch (_: SocketTimeoutException) {
            ForgeCityTtsDiagnostics.warn("gemini_audio_timeout")
            GeminiAudioResult.Timeout
        } catch (_: InterruptedIOException) {
            GeminiAudioResult.Timeout
        } catch (_: Exception) {
            ForgeCityTtsDiagnostics.warn("gemini_audio_unavailable")
            GeminiAudioResult.Unavailable
        } finally {
            connection?.disconnect()
        }
    }

    override fun close() {
        closed = true
    }

    private fun validatedUrl(model: String): URL? = runCatching {
        val encoded = URLEncoder.encode(model, Charsets.UTF_8.name())
            .replace("+", "%20")
        URL("https://generativelanguage.googleapis.com/v1beta/models/$encoded:generateContent")
    }.getOrNull()

    private fun buildRequestBody(prompt: String, voice: String, languageCode: String): String {
        val escapedPrompt = escapeJson(prompt)
        val escapedVoice = escapeJson(voice)
        val escapedLang = escapeJson(languageCode)
        return """{"contents":[{"role":"user","parts":[{"text":"$escapedPrompt"}]}],"generationConfig":{"responseModalities":["AUDIO"],"speechConfig":{"languageCode":"$escapedLang","voiceConfig":{"prebuiltVoiceConfig":{"voiceName":"$escapedVoice"}}}}}"""
    }

    private fun escapeJson(value: String): String = buildString(value.length + 8) {
        for (ch in value) {
            when (ch) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> if (ch.code < 0x20) {
                    append("\\u%04x".format(ch.code))
                } else {
                    append(ch)
                }
            }
        }
    }

    private fun parseSuccess(json: String): GeminiAudioResult {
        val audio = GeminiAudioResponseParser.extract(json) ?: run {
            ForgeCityTtsDiagnostics.warn("gemini_audio_malformed", "reason=no_inline_data")
            return GeminiAudioResult.Malformed
        }
        if (audio.pcm.isEmpty()) {
            ForgeCityTtsDiagnostics.warn("gemini_audio_malformed", "reason=empty_pcm")
            return GeminiAudioResult.Malformed
        }
        if (audio.pcm.size < 64) {
            ForgeCityTtsDiagnostics.warn("gemini_audio_malformed", "reason=pcm_too_short")
            return GeminiAudioResult.Malformed
        }
        ForgeCityTtsDiagnostics.info(
            "gemini_audio_ok",
            "bytes=${audio.pcm.size} rateHz=${audio.sampleRateHz}",
        )
        return GeminiAudioResult.Success(audio.pcm, audio.sampleRateHz, audio.mimeType)
    }

    private fun readBounded(input: java.io.InputStream?): ByteArray? {
        if (input == null) return null
        input.use { stream ->
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(8_192)
            var total = 0
            while (true) {
                val count = stream.read(buffer)
                if (count < 0) break
                total += count
                if (total > maxResponseBytes) return null
                output.write(buffer, 0, count)
            }
            return output.toByteArray()
        }
    }

    companion object {
        const val DEFAULT_TTS_MODEL = "gemini-3.1-flash-tts-preview"
        const val DEFAULT_VOICE = "Kore"
        const val DEFAULT_LANGUAGE = "ta-IN"

        /** Text-only models previously used for rewrite — migrate to TTS default. */
        private val TEXT_ONLY_MODELS = setOf(
            "gemini-2.0-flash",
            "gemini-2.0-flash-001",
            "gemini-2.0-flash-exp",
            "gemini-2.0-flash-lite",
            "gemini-2.0-flash-lite-001",
            "gemini-2.5-flash",
            "gemini-2.5-flash-lite",
            "gemini-2.5-pro",
            "gemini-flash-latest",
            "gemini-pro-latest",
        )

        fun normalizeTtsModel(raw: String): String {
            val model = raw.trim().removePrefix("models/").ifBlank { DEFAULT_TTS_MODEL }
            return if (model.lowercase() in TEXT_ONLY_MODELS) DEFAULT_TTS_MODEL else model
        }
    }
}

sealed interface GeminiAudioResult {
    data class Success(
        val pcm: ByteArray,
        val sampleRateHz: Int,
        val mimeType: String,
    ) : GeminiAudioResult {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Success) return false
            return sampleRateHz == other.sampleRateHz &&
                mimeType == other.mimeType &&
                pcm.contentEquals(other.pcm)
        }

        override fun hashCode(): Int {
            var result = pcm.contentHashCode()
            result = 31 * result + sampleRateHz
            result = 31 * result + mimeType.hashCode()
            return result
        }
    }

    data object Unavailable : GeminiAudioResult
    data object ModelUnavailable : GeminiAudioResult
    data object Timeout : GeminiAudioResult
    data object Unauthorized : GeminiAudioResult
    data object Malformed : GeminiAudioResult
}

data class GeminiAudioPayload(
    val pcm: ByteArray,
    val sampleRateHz: Int,
    val mimeType: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GeminiAudioPayload) return false
        return sampleRateHz == other.sampleRateHz &&
            mimeType == other.mimeType &&
            pcm.contentEquals(other.pcm)
    }

    override fun hashCode(): Int {
        var result = pcm.contentHashCode()
        result = 31 * result + sampleRateHz
        result = 31 * result + mimeType.hashCode()
        return result
    }
}

internal object GeminiAudioResponseParser {
    private val RATE_REGEX = Regex("""rate\s*=\s*(\d+)""", RegexOption.IGNORE_CASE)

    fun extract(json: String): GeminiAudioPayload? {
        val region = inlineDataRegion(json) ?: json
        val mimeType = extractJsonString(region, "mimeType")
            ?: extractJsonString(region, "mime_type")
            ?: "audio/L16;rate=24000"
        val b64 = extractJsonString(region, "data") ?: return null
        val pcm = runCatching {
            Base64.getDecoder().decode(b64)
        }.getOrNull() ?: return null
        val rate = RATE_REGEX.find(mimeType)?.groupValues?.getOrNull(1)?.toIntOrNull()
            ?: 24_000
        return GeminiAudioPayload(
            pcm = pcm,
            sampleRateHz = rate.coerceIn(8_000, 48_000),
            mimeType = mimeType,
        )
    }

    private fun inlineDataRegion(json: String): String? {
        val markers = listOf("\"inlineData\"", "\"inline_data\"")
        for (marker in markers) {
            val start = json.indexOf(marker)
            if (start < 0) continue
            var brace = json.indexOf('{', start)
            if (brace < 0) continue
            var depth = 0
            for (i in brace until json.length) {
                when (json[i]) {
                    '{' -> depth++
                    '}' -> {
                        depth--
                        if (depth == 0) return json.substring(brace, i + 1)
                    }
                }
            }
        }
        return null
    }

    /** Finds first JSON string value for a given key. */
    fun extractJsonString(json: String, key: String): String? {
        val marker = "\"$key\""
        var index = json.indexOf(marker)
        while (index >= 0) {
            var cursor = index + marker.length
            while (cursor < json.length && json[cursor].isWhitespace()) cursor++
            if (cursor < json.length && json[cursor] == ':') {
                cursor++
                while (cursor < json.length && json[cursor].isWhitespace()) cursor++
                if (cursor < json.length && json[cursor] == '"') {
                    cursor++
                    val output = StringBuilder()
                    while (cursor < json.length) {
                        when (val ch = json[cursor++]) {
                            '"' -> return output.toString()
                            '\\' -> {
                                if (cursor >= json.length) return null
                                when (val escaped = json[cursor++]) {
                                    '"', '\\', '/' -> output.append(escaped)
                                    'n' -> output.append('\n')
                                    'r' -> output.append('\r')
                                    't' -> output.append('\t')
                                    'u' -> {
                                        if (cursor + 4 > json.length) return null
                                        val code = json.substring(cursor, cursor + 4)
                                            .toIntOrNull(16) ?: return null
                                        output.append(code.toChar())
                                        cursor += 4
                                    }
                                    else -> return null
                                }
                            }
                            else -> output.append(ch)
                        }
                    }
                    return null
                }
            }
            index = json.indexOf(marker, index + marker.length)
        }
        return null
    }
}
