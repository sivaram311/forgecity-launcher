package buzz.delena.forgecity.assistant.rewrite

data class RewriteRequest(
    val notificationKey: String,
    val appLabel: String,
    val title: String,
    val body: String,
) {
    fun toJson(): String {
        return buildString {
            append('{')
            appendJson("schemaVersion", 1)
            append(',')
            appendJson("appLabel", appLabel.take(MAX_APP_LABEL))
            append(',')
            appendJson("title", title.take(MAX_TITLE))
            append(',')
            appendJson("text", body.take(MAX_TEXT))
            append(',')
            appendJson("maxChars", RewriteResponseParser.MAX_TAMIL_CHARS)
            append('}')
        }
    }

    private fun StringBuilder.appendJson(key: String, value: String) {
        append('"').append(key).append('"').append(':')
        append('"').append(escapeJson(value)).append('"')
    }

    private fun StringBuilder.appendJson(key: String, value: Int) {
        append('"').append(key).append('"').append(':').append(value)
    }

    private fun escapeJson(value: String): String = buildString(value.length + 8) {
        for (ch in value) {
            when (ch) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\b' -> append("\\b")
                '\u000C' -> append("\\f")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> if (ch.code < 0x20) {
                    append("\\u").append("%04x".format(ch.code))
                } else {
                    append(ch)
                }
            }
        }
    }

    private companion object {
        const val MAX_APP_LABEL = 80
        const val MAX_TITLE = 200
        const val MAX_TEXT = 2_000
    }
}

sealed interface RewriteResult {
    data class Success(val tamilText: String) : RewriteResult
    data object Unavailable : RewriteResult
    data object Timeout : RewriteResult
    data object Unauthorized : RewriteResult
    data object Malformed : RewriteResult
    data object Cancelled : RewriteResult
}

object RewriteResponseParser {
    const val MAX_TAMIL_CHARS = 220
    private val tamil = Regex("[\u0B80-\u0BFF]")
    private val forbiddenMeta = Regex(
        "(?i)(```|https?://|translation\\s*:|tamil\\s*:|prompt\\s*:|system\\s*:|" +
            "assistant\\s*:|language model|as an ai)",
    )

    fun parse(json: String): RewriteResult {
        val values = parseStrictObject(json) ?: return RewriteResult.Malformed
        if (values.keys != setOf("schemaVersion", "status", "tamil") ||
            values["schemaVersion"] != "1" ||
            values["status"] != "ok"
        ) {
            return RewriteResult.Malformed
        }
        val text = values["tamil"].orEmpty().trim()
        if (text.isEmpty() ||
            text.length > MAX_TAMIL_CHARS ||
            !tamil.containsMatchIn(text) ||
            forbiddenMeta.containsMatchIn(text)
        ) {
            return RewriteResult.Malformed
        }
        return RewriteResult.Success(text)
    }

    private fun parseStrictObject(json: String): Map<String, String>? {
        val trimmed = json.trim()
        if (!trimmed.startsWith('{') || !trimmed.endsWith('}')) return null
        val body = trimmed.substring(1, trimmed.length - 1).trim()
        if (body.isEmpty()) return null
        val result = linkedMapOf<String, String>()
        var index = 0
        while (index < body.length) {
            while (index < body.length && body[index].isWhitespace()) index++
            if (index >= body.length) return null
            if (body[index] != '"') return null
            val keyEnd = findStringEnd(body, index) ?: return null
            val key = decodeJsonString(body.substring(index + 1, keyEnd)) ?: return null
            index = keyEnd + 1
            while (index < body.length && body[index].isWhitespace()) index++
            if (index >= body.length || body[index] != ':') return null
            index++
            while (index < body.length && body[index].isWhitespace()) index++
            if (index >= body.length) return null
            val value: String
            when {
                body[index] == '"' -> {
                    val valueEnd = findStringEnd(body, index) ?: return null
                    value = decodeJsonString(body.substring(index + 1, valueEnd)) ?: return null
                    index = valueEnd + 1
                }
                body[index].isDigit() || body[index] == '-' -> {
                    val start = index
                    while (index < body.length && (body[index].isDigit() || body[index] == '-')) index++
                    value = body.substring(start, index)
                }
                else -> return null
            }
            if (key in result) return null
            result[key] = value
            while (index < body.length && body[index].isWhitespace()) index++
            if (index >= body.length) break
            if (body[index] != ',') return null
            index++
        }
        return result
    }

    private fun findStringEnd(source: String, startQuote: Int): Int? {
        var index = startQuote + 1
        while (index < source.length) {
            when (source[index]) {
                '\\' -> index += 2
                '"' -> return index
                else -> index++
            }
        }
        return null
    }

    private fun decodeJsonString(value: String): String? {
        val output = StringBuilder(value.length)
        var index = 0
        while (index < value.length) {
            val char = value[index++]
            if (char.code < 0x20) return null
            if (char != '\\') {
                output.append(char)
                continue
            }
            if (index >= value.length) return null
            when (val escaped = value[index++]) {
                '"', '\\', '/' -> output.append(escaped)
                'b' -> output.append('\b')
                'f' -> output.append('\u000C')
                'n' -> output.append('\n')
                'r' -> output.append('\r')
                't' -> output.append('\t')
                'u' -> {
                    if (index + 4 > value.length) return null
                    val code = value.substring(index, index + 4).toIntOrNull(16) ?: return null
                    output.append(code.toChar())
                    index += 4
                }
                else -> return null
            }
        }
        return output.toString()
    }
}
