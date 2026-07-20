package buzz.delena.forgecity.assistant

enum class AssistantSpeechMode {
    OFF,
    DIRECT_TTS,
    AGENT_PORTAL_TAMIL,
    SMART_CASCADE;

    fun next(): AssistantSpeechMode = entries[(ordinal + 1) % entries.size]

    companion object {
        fun fromPersisted(value: String?): AssistantSpeechMode? =
            entries.firstOrNull { it.name == value }

        fun migrate(ttsEnabled: Boolean, remoteRewriteEnabled: Boolean): AssistantSpeechMode =
            when {
                ttsEnabled && remoteRewriteEnabled -> AGENT_PORTAL_TAMIL
                ttsEnabled -> DIRECT_TTS
                else -> OFF
            }
    }
}

enum class NotificationSpeechRoute {
    NONE,
    DIRECT,
    AGENT_PORTAL_TAMIL,
    SMART_CASCADE;

    companion object {
        fun resolve(
            mode: AssistantSpeechMode,
            portalConfigured: Boolean,
        ): NotificationSpeechRoute = when (mode) {
            AssistantSpeechMode.OFF -> NONE
            AssistantSpeechMode.DIRECT_TTS -> DIRECT
            AssistantSpeechMode.AGENT_PORTAL_TAMIL ->
                if (portalConfigured) AGENT_PORTAL_TAMIL else NONE
            AssistantSpeechMode.SMART_CASCADE -> SMART_CASCADE
        }
    }
}

object LauncherChromeDefaults {
    const val VISIBLE = false
}
