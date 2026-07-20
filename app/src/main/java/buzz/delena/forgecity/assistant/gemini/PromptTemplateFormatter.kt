package buzz.delena.forgecity.assistant.gemini

object PromptTemplateDefaults {
    const val TEMPLATE =
        "You rewrite phone notifications into clear spoken Tamil.\n" +
            "Rules: keep only facts from the message, one short sentence, no English, no labels.\n" +
            "App: {appLabel}\n" +
            "Title: {title}\n" +
            "Message: {text}\n" +
            "Max length: {maxChars} characters."
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
