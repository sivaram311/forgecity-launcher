package buzz.delena.forgecity.assistant.gemini

/**
 * Named speak-aloud prompt template entry for the user library.
 */
data class PromptTemplateEntry(
    val id: String,
    val name: String,
    val body: String,
    val updatedAtMs: Long,
    val builtin: Boolean = false,
)

object PromptTemplateLibraryCodec {
    const val MAX_TEMPLATES = 50

    fun encode(entries: List<PromptTemplateEntry>): String {
        if (entries.isEmpty()) return "[]"
        return entries.joinToString(prefix = "[", postfix = "]", separator = ",") { e ->
            """{"id":${jsonStr(e.id)},"name":${jsonStr(e.name)},"body":${jsonStr(e.body)},"updatedAtMs":${e.updatedAtMs},"builtin":${e.builtin}}"""
        }
    }

    fun decode(raw: String?): List<PromptTemplateEntry> {
        val text = raw?.trim().orEmpty()
        if (text.isEmpty() || text == "[]") return emptyList()
        val out = ArrayList<PromptTemplateEntry>()
        var i = 0
        while (i < text.length) {
            val start = text.indexOf('{', i)
            if (start < 0) break
            val end = findObjectEnd(text, start) ?: break
            val obj = text.substring(start, end + 1)
            val id = extractString(obj, "id") ?: continue
            val name = extractString(obj, "name") ?: continue
            val body = extractString(obj, "body") ?: continue
            val updated = extractLong(obj, "updatedAtMs") ?: 0L
            val builtin = extractBool(obj, "builtin")
            out += PromptTemplateEntry(id, name, body, updated, builtin)
            i = end + 1
        }
        return out
    }

    fun seedFromPresets(nowMs: Long = System.currentTimeMillis()): List<PromptTemplateEntry> =
        AudioPromptPresets.ALL.mapIndexed { index, preset ->
            PromptTemplateEntry(
                id = preset.id,
                name = preset.label,
                body = preset.template,
                updatedAtMs = nowMs - index,
                builtin = true,
            )
        }

    private fun jsonStr(value: String): String {
        val escaped = buildString(value.length + 8) {
            append('"')
            value.forEach { c ->
                when (c) {
                    '\\' -> append("\\\\")
                    '"' -> append("\\\"")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> append(c)
                }
            }
            append('"')
        }
        return escaped
    }

    private fun findObjectEnd(text: String, start: Int): Int? {
        var depth = 0
        var inStr = false
        var escape = false
        for (i in start until text.length) {
            val c = text[i]
            if (inStr) {
                when {
                    escape -> escape = false
                    c == '\\' -> escape = true
                    c == '"' -> inStr = false
                }
                continue
            }
            when (c) {
                '"' -> inStr = true
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) return i
                }
            }
        }
        return null
    }

    private fun extractString(obj: String, key: String): String? {
        val marker = "\"$key\""
        val idx = obj.indexOf(marker)
        if (idx < 0) return null
        val colon = obj.indexOf(':', idx + marker.length)
        if (colon < 0) return null
        var i = colon + 1
        while (i < obj.length && obj[i].isWhitespace()) i++
        if (i >= obj.length || obj[i] != '"') return null
        i++
        val sb = StringBuilder()
        while (i < obj.length) {
            val c = obj[i]
            when {
                c == '\\' && i + 1 < obj.length -> {
                    when (obj[i + 1]) {
                        'n' -> sb.append('\n')
                        'r' -> sb.append('\r')
                        't' -> sb.append('\t')
                        '"' -> sb.append('"')
                        '\\' -> sb.append('\\')
                        else -> sb.append(obj[i + 1])
                    }
                    i += 2
                }
                c == '"' -> return sb.toString()
                else -> {
                    sb.append(c)
                    i++
                }
            }
        }
        return null
    }

    private fun extractLong(obj: String, key: String): Long? {
        val marker = "\"$key\""
        val idx = obj.indexOf(marker)
        if (idx < 0) return null
        val colon = obj.indexOf(':', idx + marker.length)
        if (colon < 0) return null
        val rest = obj.substring(colon + 1).trimStart()
        val digits = rest.takeWhile { it.isDigit() || it == '-' }
        return digits.toLongOrNull()
    }

    private fun extractBool(obj: String, key: String): Boolean {
        val marker = "\"$key\""
        val idx = obj.indexOf(marker)
        if (idx < 0) return false
        val colon = obj.indexOf(':', idx + marker.length)
        if (colon < 0) return false
        val rest = obj.substring(colon + 1).trimStart()
        return rest.startsWith("true")
    }
}
