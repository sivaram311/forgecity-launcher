package buzz.delena.forgecity.assistant

import android.content.Context

/**
 * Privacy-first assistant prefs. Never stores notification title/body.
 * Defaults: TTS off, allowlist empty.
 */
class AssistantSettingsStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    var assistantEnabled: Boolean
        get() = prefs.getBoolean(KEY_ASSISTANT, true)
        set(value) = prefs.edit().putBoolean(KEY_ASSISTANT, value).apply()

    var ttsEnabled: Boolean
        get() = prefs.getBoolean(KEY_TTS, false)
        set(value) = prefs.edit().putBoolean(KEY_TTS, value).apply()

    var quietStartMinutes: Int
        get() = prefs.getInt(KEY_QUIET_START, 22 * 60)
        set(value) = prefs.edit().putInt(KEY_QUIET_START, value).apply()

    var quietEndMinutes: Int
        get() = prefs.getInt(KEY_QUIET_END, 7 * 60)
        set(value) = prefs.edit().putInt(KEY_QUIET_END, value).apply()

    var backgroundVideoEnabled: Boolean
        get() = prefs.getBoolean(KEY_BACKGROUND_VIDEO, true)
        set(value) = prefs.edit().putBoolean(KEY_BACKGROUND_VIDEO, value).apply()

    var backgroundVideoOpacity: Float
        get() = prefs.getFloat(KEY_BACKGROUND_OPACITY, 0.80f).coerceIn(0.4f, 1f)
        set(value) = prefs.edit()
            .putFloat(KEY_BACKGROUND_OPACITY, value.coerceIn(0.4f, 1f))
            .apply()

    fun allowedPackages(): Set<String> =
        prefs.getStringSet(KEY_ALLOW, emptySet())?.toSet().orEmpty()

    fun setAllowedPackages(packages: Set<String>) {
        prefs.edit().putStringSet(KEY_ALLOW, packages.toMutableSet()).apply()
    }

    fun setPackageAllowed(packageName: String, allowed: Boolean) {
        val next = allowedPackages().toMutableSet()
        if (allowed) next += packageName else next -= packageName
        setAllowedPackages(next)
    }

    companion object {
        private const val PREFS = "forgecity_assistant"
        private const val KEY_ASSISTANT = "assistant_enabled"
        private const val KEY_TTS = "tts_enabled"
        private const val KEY_ALLOW = "allowed_packages"
        private const val KEY_QUIET_START = "quiet_start"
        private const val KEY_QUIET_END = "quiet_end"
        private const val KEY_BACKGROUND_VIDEO = "background_video_enabled"
        private const val KEY_BACKGROUND_OPACITY = "background_video_opacity"
    }
}
