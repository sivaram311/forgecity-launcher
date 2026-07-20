package buzz.delena.forgecity.assistant.gemini

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Base64

class GeminiAudioTtsClientTest {
    @Test
    fun migratesTextModelsToTtsDefault() {
        assertEquals(
            GeminiAudioTtsClient.DEFAULT_TTS_MODEL,
            GeminiAudioTtsClient.normalizeTtsModel("gemini-2.5-flash"),
        )
        assertEquals(
            GeminiAudioTtsClient.DEFAULT_TTS_MODEL,
            GeminiAudioTtsClient.normalizeTtsModel("models/gemini-2.0-flash"),
        )
    }

    @Test
    fun keepsExplicitTtsModel() {
        assertEquals(
            "gemini-3.1-flash-tts-preview",
            GeminiAudioTtsClient.normalizeTtsModel("gemini-3.1-flash-tts-preview"),
        )
    }

    @Test
    fun extractsInlinePcmAndSampleRate() {
        val pcm = ByteArray(128) { it.toByte() }
        val b64 = Base64.getEncoder().encodeToString(pcm)
        val json = """
            {"candidates":[{"content":{"parts":[{"inlineData":{
              "mimeType":"audio/L16;rate=24000",
              "data":"$b64"
            }}]}}]}
        """.trimIndent()
        val payload = GeminiAudioResponseParser.extract(json)
        assertNotNull(payload)
        assertEquals(24_000, payload!!.sampleRateHz)
        assertTrue(payload.pcm.contentEquals(pcm))
    }
}

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
        assertTrue(formatted.contains("Speak a clear Tamil"))
    }
}

class PcmAudioNormalizerTest {
    @Test
    fun trimsOddRawPcmByte() {
        val raw = ByteArray(5) { it.toByte() }
        val normalized = buzz.delena.forgecity.assistant.PcmAudioNormalizer.normalize(raw, 24_000)
        assertEquals(4, normalized.pcm.size)
        assertEquals(24_000, normalized.sampleRateHz)
        assertTrue(!normalized.hadWavHeader)
    }

    @Test
    fun stripsWavHeaderAndReadsRate() {
        val pcm = ByteArray(100) { it.toByte() }
        val wav = buzz.delena.forgecity.assistant.PcmAudioNormalizer.toWav(pcm, 16_000)
        val normalized = buzz.delena.forgecity.assistant.PcmAudioNormalizer.normalize(wav, 24_000)
        assertTrue(normalized.hadWavHeader)
        assertEquals(16_000, normalized.sampleRateHz)
        assertTrue(normalized.pcm.contentEquals(pcm))
    }
}
