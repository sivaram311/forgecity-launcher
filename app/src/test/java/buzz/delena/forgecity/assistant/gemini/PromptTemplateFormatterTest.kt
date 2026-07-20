package buzz.delena.forgecity.assistant.gemini

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

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
        assertFalse(formatted.isBlank())
        assertEquals(true, formatted.contains("ForgeCity"))
    }
}
