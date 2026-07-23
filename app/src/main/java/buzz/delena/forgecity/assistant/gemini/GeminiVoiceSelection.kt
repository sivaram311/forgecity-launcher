package buzz.delena.forgecity.assistant.gemini

import kotlin.random.Random

/**
 * Persisted Gemini voice selection: fixed name or random mode sentinels in KEY_GEMINI_VOICE.
 * Random resolves to a concrete catalog voiceName on every synthesize.
 */
enum class GeminiVoiceMode {
    FIXED,
    RANDOM,
    RANDOM_FEMALE,
    RANDOM_MALE,
}

object GeminiVoiceSelection {
    const val SENTINEL_RANDOM = "__RANDOM__"
    const val SENTINEL_RANDOM_FEMALE = "__RANDOM_F__"
    const val SENTINEL_RANDOM_MALE = "__RANDOM_M__"

    fun encode(mode: GeminiVoiceMode, fixedVoice: String = GeminiAudioTtsClient.DEFAULT_VOICE): String =
        when (mode) {
            GeminiVoiceMode.RANDOM -> SENTINEL_RANDOM
            GeminiVoiceMode.RANDOM_FEMALE -> SENTINEL_RANDOM_FEMALE
            GeminiVoiceMode.RANDOM_MALE -> SENTINEL_RANDOM_MALE
            GeminiVoiceMode.FIXED -> fixedVoice.trim().ifBlank { GeminiAudioTtsClient.DEFAULT_VOICE }
        }

    fun modeOf(stored: String): GeminiVoiceMode = when (stored.trim()) {
        SENTINEL_RANDOM -> GeminiVoiceMode.RANDOM
        SENTINEL_RANDOM_FEMALE -> GeminiVoiceMode.RANDOM_FEMALE
        SENTINEL_RANDOM_MALE -> GeminiVoiceMode.RANDOM_MALE
        else -> GeminiVoiceMode.FIXED
    }

    fun displayLabel(stored: String): String = when (modeOf(stored)) {
        GeminiVoiceMode.RANDOM -> "Random"
        GeminiVoiceMode.RANDOM_FEMALE -> "Random female"
        GeminiVoiceMode.RANDOM_MALE -> "Random male"
        GeminiVoiceMode.FIXED -> {
            val v = GeminiTtsCatalog.findVoice(stored)
            v?.label ?: stored.trim().ifBlank { GeminiAudioTtsClient.DEFAULT_VOICE }
        }
    }
}

object GeminiVoiceResolver {
    fun resolve(stored: String, rng: Random = Random.Default): String {
        val raw = stored.trim().ifBlank { GeminiAudioTtsClient.DEFAULT_VOICE }
        return when (GeminiVoiceSelection.modeOf(raw)) {
            GeminiVoiceMode.FIXED -> {
                GeminiTtsCatalog.findVoice(raw)?.name
                    ?: GeminiAudioTtsClient.DEFAULT_VOICE
            }
            GeminiVoiceMode.RANDOM -> pick(GeminiTtsCatalog.voices, rng)
            GeminiVoiceMode.RANDOM_FEMALE ->
                pick(GeminiTtsCatalog.voicesByGender(GeminiTtsCatalog.Gender.FEMALE), rng)
            GeminiVoiceMode.RANDOM_MALE ->
                pick(GeminiTtsCatalog.voicesByGender(GeminiTtsCatalog.Gender.MALE), rng)
        }
    }

    private fun pick(pool: List<GeminiTtsCatalog.VoiceOption>, rng: Random): String {
        if (pool.isEmpty()) return GeminiAudioTtsClient.DEFAULT_VOICE
        return pool[rng.nextInt(pool.size)].name
    }
}
