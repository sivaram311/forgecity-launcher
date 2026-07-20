package buzz.delena.forgecity.assistant.gemini

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeminiRewriteClientTest {
    @Test
    fun migratesShutDownDefaultModel() {
        assertEquals(
            "gemini-2.5-flash",
            GeminiRewriteClient.normalizeModel("gemini-2.0-flash"),
        )
        assertEquals(
            "gemini-2.5-flash",
            GeminiRewriteClient.normalizeModel("models/gemini-2.0-flash-001"),
        )
    }

    @Test
    fun keepsCurrentModels() {
        assertEquals("gemini-2.5-flash", GeminiRewriteClient.normalizeModel("gemini-2.5-flash"))
        assertEquals("gemini-flash-latest", GeminiRewriteClient.normalizeModel("gemini-flash-latest"))
    }

    @Test
    fun extractsCandidateText() {
        val json = """
            {"candidates":[{"content":{"parts":[{"text":"வணக்கம்"}]}}]}
        """.trimIndent()
        assertEquals("வணக்கம்", GeminiResponseParser.extractText(json))
    }
}

class PromptTemplateFormatterTest {
    @Test
    fun replacesAllPlaceholders() {
        val formatted = PromptTemplateFormatter.format(
            template = "App={appLabel} Title={title} Body={text} Max={maxChars}",
            appLabel = "Chat",
            title = "Hi",
            text = "Hello",
            maxChars = 120,
        )
        assertEquals("App=Chat Title=Hi Body=Hello Max=120", formatted)
    }

    @Test
    fun usesDefaultWhenBlank() {
        val formatted = PromptTemplateFormatter.format(
            template = "   ",
            appLabel = "ForgeCity",
            title = "Test",
            text = "Body",
        )
        assertTrue(formatted.contains("ForgeCity"))
    }
}
