package buzz.delena.forgecity.assistant.gemini

object PromptTemplateDefaults {
    /**
     * Prompt for Gemini native audio TTS (spoken output, not text rewrite).
     * Lead with an explicit synthesize-speech preamble so the TTS classifier
     * does not reject the request as plain text generation.
     */
    const val TEMPLATE =
        "Synthesize speech only (audio output, no text). Read aloud in clear Tamil.\n" +
            "Rules: one short sentence, facts only from the message, no English labels, natural pace.\n" +
            "#### TRANSCRIPT\n" +
            "App: {appLabel}. Title: {title}. Message: {text}\n" +
            "Keep spoken content under {maxChars} characters."
}

object PromptTemplateFormatter {
    fun format(
        template: String,
        appLabel: String,
        title: String,
        text: String,
        maxChars: Int = 220,
    ): String {
        val base = template.ifBlank { PromptTemplateDefaults.TEMPLATE }
        return base
            .replace("{appLabel}", appLabel.take(80))
            .replace("{title}", title.take(200))
            .replace("{text}", text.take(2_000))
            .replace("{maxChars}", maxChars.toString())
    }
}
