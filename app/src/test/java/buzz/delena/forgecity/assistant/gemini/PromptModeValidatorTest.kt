package buzz.delena.forgecity.assistant.gemini

import buzz.delena.forgecity.assistant.AssistantSpeechMode
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PromptModeValidatorTest {
    private val rewritePrompt =
        """
        You are a friendly Kongu Tamil speaker.
        Output format:
        Provide ONLY the final spoken script text for TTS.
        App: {appLabel}
        Title: {title}
        Message: {text}
        """.trimIndent()

    @Test
    fun directAndPortalAlwaysOk() {
        assertTrue(PromptModeValidator.validate(AssistantSpeechMode.DIRECT_TTS, rewritePrompt).ok)
        assertTrue(
            PromptModeValidator.validate(AssistantSpeechMode.AGENT_PORTAL_TAMIL, rewritePrompt).ok,
        )
        assertFalse(PromptModeValidator.canRunTest(AssistantSpeechMode.OFF, rewritePrompt))
    }

    @Test
    fun geminiAudioRejectsRewritePrompt() {
        val result = PromptModeValidator.validate(AssistantSpeechMode.GEMINI_TAMIL, rewritePrompt)
        assertFalse(result.ok)
        assertTrue(result.message!!.contains("rewrite", ignoreCase = true) ||
            result.message!!.contains("script", ignoreCase = true))
        assertFalse(PromptModeValidator.canRunTest(AssistantSpeechMode.GEMINI_TAMIL, rewritePrompt))
    }

    @Test
    fun geminiAudioAcceptsDefaultAndKonguPresets() {
        assertTrue(
            PromptModeValidator.validate(
                AssistantSpeechMode.GEMINI_TAMIL,
                PromptTemplateDefaults.TEMPLATE,
            ).ok,
        )
        val kongu = AudioPromptPresets.byId("kongu_friend")!!.template
        assertTrue(PromptModeValidator.validate(AssistantSpeechMode.GEMINI_TAMIL, kongu).ok)
        assertTrue(PromptModeValidator.canRunTest(AssistantSpeechMode.SMART_CASCADE, kongu))
    }

    @Test
    fun cascadeRequiresAudioCue() {
        val bare = "App {appLabel} title {title} text {text}"
        val result = PromptModeValidator.validate(AssistantSpeechMode.SMART_CASCADE, bare)
        assertFalse(result.ok)
        assertTrue(result.message!!.contains("Synthesize", ignoreCase = true) ||
            result.message!!.contains("Read aloud", ignoreCase = true))
    }
}
