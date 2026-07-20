package buzz.delena.forgecity.assistant.gemini

import buzz.delena.forgecity.assistant.ForgeCityTtsDiagnostics
import buzz.delena.forgecity.assistant.rewrite.RewriteResponseParser
import java.io.ByteArrayOutputStream
import java.io.InterruptedIOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder
import javax.net.ssl.HttpsURLConnection

class GeminiRewriteClient(
    private val connectTimeoutMs: Int = 8_000,
    private val readTimeoutMs: Int = 30_000,
    private val maxResponseBytes: Int = 32 * 1024,
) : AutoCloseable {
    @Volatile
    private var closed = false

    fun rewrite(apiKey: String, model: String, prompt: String): GeminiRewriteResult {
        if (closed || apiKey.isBlank() || prompt.isBlank()) {
            ForgeCityTtsDiagnostics.warn("gemini_blocked", "reason=closed_or_blank")
            return GeminiRewriteResult.Unavailable
        }
        val modelId = normalizeModel(model)
        val url = validatedUrl(modelId) ?: return GeminiRewriteResult.Unavailable
        val startedAt = System.currentTimeMillis()
        ForgeCityTtsDiagnostics.info("gemini_start", "model=$modelId")
        var connection: HttpsURLConnection? = null
        return try {
            connection = (url.openConnection() as? HttpsURLConnection)
                ?: return GeminiRewriteResult.Unavailable
            connection.requestMethod = "POST"
            connection.connectTimeout = connectTimeoutMs
            connection.readTimeout = readTimeoutMs
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            // Prefer header over query-string so keys with reserved URL chars still work.
            connection.setRequestProperty("x-goog-api-key", apiKey.trim())
            val payload = buildRequestBody(prompt).toByteArray(Charsets.UTF_8)
            connection.setFixedLengthStreamingMode(payload.size)
            connection.outputStream.use { it.write(payload) }
            val status = connection.responseCode
            ForgeCityTtsDiagnostics.info(
                "gemini_http",
                "status=$status model=$modelId elapsedMs=${System.currentTimeMillis() - startedAt}",
            )
            when (status) {
                HttpURLConnection.HTTP_OK -> {
                    val body = readBounded(connection.inputStream)
                        ?: return GeminiRewriteResult.Malformed
                    parseSuccess(body.toString(Charsets.UTF_8))
                }
                HttpURLConnection.HTTP_UNAUTHORIZED,
                HttpURLConnection.HTTP_FORBIDDEN,
                -> GeminiRewriteResult.Unauthorized
                HttpURLConnection.HTTP_BAD_REQUEST -> {
                    val err = readBounded(connection.errorStream)?.toString(Charsets.UTF_8).orEmpty()
                    when {
                        err.contains("API_KEY_INVALID", ignoreCase = true) ||
                            err.contains("API key not valid", ignoreCase = true) ->
                            GeminiRewriteResult.Unauthorized
                        err.contains("not found", ignoreCase = true) ||
                            err.contains("NOT_FOUND", ignoreCase = true) ->
                            GeminiRewriteResult.ModelUnavailable
                        else -> {
                            ForgeCityTtsDiagnostics.warn("gemini_bad_request", "status=400")
                            GeminiRewriteResult.Unavailable
                        }
                    }
                }
                HttpURLConnection.HTTP_NOT_FOUND -> GeminiRewriteResult.ModelUnavailable
                HttpURLConnection.HTTP_GATEWAY_TIMEOUT,
                429,
                -> GeminiRewriteResult.Timeout
                else -> GeminiRewriteResult.Unavailable
            }
        } catch (_: SocketTimeoutException) {
            ForgeCityTtsDiagnostics.warn("gemini_timeout")
            GeminiRewriteResult.Timeout
        } catch (_: InterruptedIOException) {
            GeminiRewriteResult.Timeout
        } catch (_: Exception) {
            ForgeCityTtsDiagnostics.warn("gemini_unavailable")
            GeminiRewriteResult.Unavailable
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

    private fun buildRequestBody(prompt: String): String {
        val escaped = escapeJson(prompt)
        return """{"contents":[{"role":"user","parts":[{"text":"$escaped"}]}],"generationConfig":{"temperature":0.2,"maxOutputTokens":256}}"""
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

    private fun parseSuccess(json: String): GeminiRewriteResult {
        val text = GeminiResponseParser.extractText(json)?.trim().orEmpty()
        if (text.isEmpty()) {
            ForgeCityTtsDiagnostics.warn("gemini_malformed", "reason=empty_text")
            return GeminiRewriteResult.Malformed
        }
        if (text.length > RewriteResponseParser.MAX_TAMIL_CHARS) {
            ForgeCityTtsDiagnostics.warn("gemini_malformed", "reason=too_long len=${text.length}")
            return GeminiRewriteResult.Malformed
        }
        val tamil = Regex("[\\u0B80-\\u0BFF]")
        if (!tamil.containsMatchIn(text)) {
            ForgeCityTtsDiagnostics.warn("gemini_malformed", "reason=no_tamil_script")
            return GeminiRewriteResult.Malformed
        }
        return GeminiRewriteResult.Success(text)
    }

    private fun readBounded(input: java.io.InputStream?): ByteArray? {
        if (input == null) return null
        input.use { stream ->
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(2_048)
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
        /** gemini-2.0-flash was shut down 2026-06-01 — do not use as default. */
        const val DEFAULT_MODEL = "gemini-2.5-flash"

        private val SHUT_DOWN_MODELS = setOf(
            "gemini-2.0-flash",
            "gemini-2.0-flash-001",
            "gemini-2.0-flash-exp",
            "gemini-2.0-flash-lite",
            "gemini-2.0-flash-lite-001",
        )

        fun normalizeModel(raw: String): String {
            val model = raw.trim().removePrefix("models/").ifBlank { DEFAULT_MODEL }
            return if (model.lowercase() in SHUT_DOWN_MODELS) DEFAULT_MODEL else model
        }
    }
}

sealed interface GeminiRewriteResult {
    data class Success(val tamilText: String) : GeminiRewriteResult
    data object Unavailable : GeminiRewriteResult
    data object ModelUnavailable : GeminiRewriteResult
    data object Timeout : GeminiRewriteResult
    data object Unauthorized : GeminiRewriteResult
    data object Malformed : GeminiRewriteResult
}

internal object GeminiResponseParser {
    fun extractText(json: String): String? {
        // Prefer candidates[0].content.parts[*].text; fall back to first "text" string.
        val partsMarker = "\"parts\""
        val partsIndex = json.indexOf(partsMarker)
        val searchFrom = if (partsIndex >= 0) partsIndex else 0
        var index = json.indexOf("\"text\"", searchFrom)
        if (index < 0) index = json.indexOf("\"text\"")
        if (index < 0) return null
        var cursor = index + "\"text\"".length
        while (cursor < json.length && json[cursor].isWhitespace()) cursor++
        if (cursor >= json.length || json[cursor] != ':') return null
        cursor++
        while (cursor < json.length && json[cursor].isWhitespace()) cursor++
        if (cursor >= json.length || json[cursor] != '"') return null
        cursor++
        val output = StringBuilder()
        while (cursor < json.length) {
            when (val ch = json[cursor++]) {
                '"' -> return output.toString().trim()
                '\\' -> {
                    if (cursor >= json.length) return null
                    when (val escaped = json[cursor++]) {
                        '"', '\\', '/' -> output.append(escaped)
                        'n' -> output.append('\n')
                        'r' -> output.append('\r')
                        't' -> output.append('\t')
                        'u' -> {
                            if (cursor + 4 > json.length) return null
                            val code = json.substring(cursor, cursor + 4).toIntOrNull(16) ?: return null
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
