package buzz.delena.forgecity.assistant.gemini

import buzz.delena.forgecity.assistant.AssistantSpeechMode

/**
 * Keeps GEMINI AUDIO / CASCADE prompts in "speak aloud" shape so
 * `responseModalities=AUDIO` does not return HTTP 400 invalid_argument.
 *
 * Placeholders `{appLabel}` `{title}` `{text}` `{maxChars}` are allowed.
 * Rewrite-style instructions (produce script text) are rejected for audio modes.
 */
object PromptModeValidator {
    private val REWRITE_MARKERS = listOf(
        "output format",
        "spoken script",
        "script text",
        "provide only the final",
        "for tts",
        "text-to-speech) to pronounce",
        "do not include quotes",
        "rewrite",
        "translate the notification",
        "return only text",
        "respond with text",
    )

    private val AUDIO_MARKERS = listOf(
        "synthesize speech",
        "read aloud",
        "audio output",
        "speak aloud",
        "#### transcript",
    )

    data class Result(
        val ok: Boolean,
        val message: String? = null,
    )

    fun needsAudioSpeakPrompt(mode: AssistantSpeechMode): Boolean =
        mode == AssistantSpeechMode.GEMINI_TAMIL ||
            mode == AssistantSpeechMode.SMART_CASCADE

    fun validate(mode: AssistantSpeechMode, template: String): Result {
        if (!needsAudioSpeakPrompt(mode)) return Result(ok = true)
        val text = template.trim()
        if (text.isEmpty()) {
            return Result(ok = false, message = "Add a speak-aloud prompt (or pick a preset).")
        }
        val lower = text.lowercase()
        val rewriteHit = REWRITE_MARKERS.firstOrNull { lower.contains(it) }
        if (rewriteHit != null) {
            return Result(
                ok = false,
                message = "Looks like a rewrite prompt (“$rewriteHit”). " +
                    "GEMINI AUDIO needs speak-aloud text, not script output.",
            )
        }
        val hasAudioCue = AUDIO_MARKERS.any { lower.contains(it) }
        if (!hasAudioCue) {
            return Result(
                ok = false,
                message = "Start with “Synthesize speech” or “Read aloud” for native audio.",
            )
        }
        return Result(ok = true)
    }

    fun canRunTest(mode: AssistantSpeechMode, template: String): Boolean =
        when (mode) {
            AssistantSpeechMode.OFF -> false
            AssistantSpeechMode.DIRECT_TTS,
            AssistantSpeechMode.AGENT_PORTAL_TAMIL,
            -> true
            AssistantSpeechMode.GEMINI_TAMIL,
            AssistantSpeechMode.SMART_CASCADE,
            -> validate(mode, template).ok
        }
}

object AudioPromptPresets {
    data class Preset(val id: String, val label: String, val template: String)

    val ALL: List<Preset> = listOf(
        Preset(
            id = "tamil_clear",
            label = "Tamil clear",
            template = PromptTemplateDefaults.TEMPLATE,
        ),
        Preset(
            id = "kongu_friend",
            label = "Kongu friend",
            template =
                "Synthesize speech only (audio output, no text). Read aloud as a warm Kongu Tamil " +
                    "(Coimbatore) friend speaking to Siva.\n" +
                    "Style: natural spoken Kongu Tamil with -nga warmth; keep app names, amounts, OTPs " +
                    "in English; one short update; no quotes, labels, or explanations.\n" +
                    "#### TRANSCRIPT\n" +
                    "App: {appLabel}. Title: {title}. Message: {text}\n" +
                    "Keep spoken content under {maxChars} characters.",
        ),
        Preset(
            id = "english_brief",
            label = "English brief",
            template =
                "Synthesize speech only (audio output, no text). Read aloud in clear English.\n" +
                    "Rules: one short sentence, facts only from the message, natural pace.\n" +
                    "#### TRANSCRIPT\n" +
                    "App: {appLabel}. Title: {title}. Message: {text}\n" +
                    "Keep spoken content under {maxChars} characters.",
        ),
    )

    fun byId(id: String): Preset? = ALL.firstOrNull { it.id == id }
}
