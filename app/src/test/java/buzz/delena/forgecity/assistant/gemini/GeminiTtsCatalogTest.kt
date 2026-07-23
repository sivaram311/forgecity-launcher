package buzz.delena.forgecity.assistant.gemini

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GeminiTtsCatalogTest {
    @Test
    fun thirtyVoicesWithBothGenders() {
        assertEquals(30, GeminiTtsCatalog.voices.size)
        assertTrue(GeminiTtsCatalog.voicesByGender(GeminiTtsCatalog.Gender.FEMALE).isNotEmpty())
        assertTrue(GeminiTtsCatalog.voicesByGender(GeminiTtsCatalog.Gender.MALE).isNotEmpty())
        assertTrue(GeminiTtsCatalog.modelIds().contains(GeminiAudioTtsClient.DEFAULT_TTS_MODEL))
    }
}

class GeminiVoiceResolverTest {
    @Test
    fun fixedPassesThrough() {
        assertEquals("Puck", GeminiVoiceResolver.resolve("Puck", Random(1)))
        assertEquals("Kore", GeminiVoiceResolver.resolve("  Kore  ", Random(1)))
    }

    @Test
    fun randomSentinelsPickFromPools() {
        val females = GeminiTtsCatalog.voicesByGender(GeminiTtsCatalog.Gender.FEMALE).map { it.name }.toSet()
        val males = GeminiTtsCatalog.voicesByGender(GeminiTtsCatalog.Gender.MALE).map { it.name }.toSet()
        repeat(20) { i ->
            val f = GeminiVoiceResolver.resolve(GeminiVoiceSelection.SENTINEL_RANDOM_FEMALE, Random(i))
            val m = GeminiVoiceResolver.resolve(GeminiVoiceSelection.SENTINEL_RANDOM_MALE, Random(i))
            val a = GeminiVoiceResolver.resolve(GeminiVoiceSelection.SENTINEL_RANDOM, Random(i))
            assertTrue(f in females)
            assertTrue(m in males)
            assertTrue(a in GeminiTtsCatalog.voiceNames())
        }
    }

    @Test
    fun encodeDecodeModes() {
        assertEquals(
            GeminiVoiceMode.RANDOM_FEMALE,
            GeminiVoiceSelection.modeOf(GeminiVoiceSelection.encode(GeminiVoiceMode.RANDOM_FEMALE)),
        )
        assertEquals(
            "Kore",
            GeminiVoiceSelection.encode(GeminiVoiceMode.FIXED, "Kore"),
        )
    }
}

class PromptTemplateLibraryCodecTest {
    @Test
    fun roundTrip() {
        val entries = listOf(
            PromptTemplateEntry("a", "A", "Synthesize speech\nline", 1L, true),
            PromptTemplateEntry("b", "B \"x\"", "body", 2L, false),
        )
        val json = PromptTemplateLibraryCodec.encode(entries)
        val back = PromptTemplateLibraryCodec.decode(json)
        assertEquals(2, back.size)
        assertEquals("a", back[0].id)
        assertEquals("Synthesize speech\nline", back[0].body)
        assertEquals("B \"x\"", back[1].name)
    }

    @Test
    fun seedHasThreeBuiltins() {
        val seed = PromptTemplateLibraryCodec.seedFromPresets(1000L)
        assertEquals(3, seed.size)
        assertTrue(seed.all { it.builtin })
    }
}
