package buzz.delena.forgecity.assistant.gemini

object PromptTemplateDefaults {
    /** Prompt for Gemini native audio TTS (spoken output, not text rewrite). */
    const val TEMPLATE =
        "Speak a clear Tamil summary of this phone notification for the listener.\n" +
            "Rules: one short sentence, facts only from the message, no English, no labels, natural pace.\n" +
            "App: {appLabel}\n" +
            "Title: {title}\n" +
            "Message: {text}\n" +
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
