package buzz.delena.forgecity.assistant.gemini

/**
 * Curated Gemini TTS models + 30 prebuilt voices (no network fetch).
 * Voice genders from Google Cloud Gemini-TTS docs.
 */
object GeminiTtsCatalog {
    enum class Gender { FEMALE, MALE }

    data class ModelOption(
        val id: String,
        val label: String,
    )

    data class VoiceOption(
        val name: String,
        val style: String,
        val gender: Gender,
    ) {
        val label: String get() = "$name — $style"
    }

    val models: List<ModelOption> = listOf(
        ModelOption(GeminiAudioTtsClient.DEFAULT_TTS_MODEL, "3.1 Flash TTS (default)"),
        ModelOption("gemini-2.5-flash-preview-tts", "2.5 Flash TTS"),
        ModelOption("gemini-2.5-pro-preview-tts", "2.5 Pro TTS"),
    )

    val voices: List<VoiceOption> = listOf(
        VoiceOption("Zephyr", "Bright", Gender.FEMALE),
        VoiceOption("Puck", "Upbeat", Gender.MALE),
        VoiceOption("Charon", "Informative", Gender.MALE),
        VoiceOption("Kore", "Firm", Gender.FEMALE),
        VoiceOption("Fenrir", "Excitable", Gender.MALE),
        VoiceOption("Leda", "Youthful", Gender.FEMALE),
        VoiceOption("Orus", "Firm", Gender.MALE),
        VoiceOption("Aoede", "Breezy", Gender.FEMALE),
        VoiceOption("Callirrhoe", "Easy-going", Gender.FEMALE),
        VoiceOption("Autonoe", "Bright", Gender.FEMALE),
        VoiceOption("Enceladus", "Breathy", Gender.MALE),
        VoiceOption("Iapetus", "Clear", Gender.MALE),
        VoiceOption("Umbriel", "Easy-going", Gender.MALE),
        VoiceOption("Algieba", "Smooth", Gender.MALE),
        VoiceOption("Despina", "Smooth", Gender.FEMALE),
        VoiceOption("Erinome", "Clear", Gender.FEMALE),
        VoiceOption("Algenib", "Gravelly", Gender.MALE),
        VoiceOption("Rasalgethi", "Informative", Gender.MALE),
        VoiceOption("Laomedeia", "Upbeat", Gender.FEMALE),
        VoiceOption("Achernar", "Soft", Gender.FEMALE),
        VoiceOption("Alnilam", "Firm", Gender.MALE),
        VoiceOption("Schedar", "Even", Gender.MALE),
        VoiceOption("Gacrux", "Mature", Gender.FEMALE),
        VoiceOption("Pulcherrima", "Forward", Gender.FEMALE),
        VoiceOption("Achird", "Friendly", Gender.MALE),
        VoiceOption("Zubenelgenubi", "Casual", Gender.MALE),
        VoiceOption("Vindemiatrix", "Gentle", Gender.FEMALE),
        VoiceOption("Sadachbia", "Lively", Gender.FEMALE),
        VoiceOption("Sadaltager", "Knowledgeable", Gender.MALE),
        VoiceOption("Sulafat", "Warm", Gender.FEMALE),
    )

    fun modelIds(): List<String> = models.map { it.id }

    fun voiceNames(): List<String> = voices.map { it.name }

    fun voicesByGender(gender: Gender): List<VoiceOption> =
        voices.filter { it.gender == gender }

    fun findVoice(name: String): VoiceOption? =
        voices.firstOrNull { it.name.equals(name.trim(), ignoreCase = true) }
}
