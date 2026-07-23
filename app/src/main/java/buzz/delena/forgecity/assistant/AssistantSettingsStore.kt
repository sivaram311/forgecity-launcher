package buzz.delena.forgecity.assistant

import android.content.Context
import buzz.delena.forgecity.assistant.gemini.GeminiAudioTtsClient
import buzz.delena.forgecity.assistant.gemini.AudioPromptPresets
import buzz.delena.forgecity.assistant.gemini.PromptTemplateDefaults
import buzz.delena.forgecity.assistant.gemini.PromptTemplateEntry
import buzz.delena.forgecity.assistant.gemini.PromptTemplateLibraryCodec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Privacy-first assistant prefs. Never stores notification title/body.
 * Defaults: speech off, background video off, launcher chrome hidden, allowlist empty.
 */
class AssistantSettingsStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    var assistantEnabled: Boolean
        get() = prefs.getBoolean(KEY_ASSISTANT, true)
        set(value) = prefs.edit().putBoolean(KEY_ASSISTANT, value).apply()

    var speechMode: AssistantSpeechMode
        get() {
            AssistantSpeechMode.fromPersisted(prefs.getString(KEY_SPEECH_MODE, null))
                ?.let { return it }
            val migrated = AssistantSpeechMode.migrate(
                ttsEnabled = prefs.getBoolean(KEY_TTS, false),
                remoteRewriteEnabled = prefs.getBoolean(KEY_REMOTE_REWRITE, false),
            )
            prefs.edit().putString(KEY_SPEECH_MODE, migrated.name).apply()
            return migrated
        }
        set(value) = prefs.edit().putString(KEY_SPEECH_MODE, value.name).apply()

    var rewriteEndpoint: String
        get() = prefs.getString(KEY_REWRITE_ENDPOINT, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_REWRITE_ENDPOINT, value.trim()).apply()

    val hasApiKey: Boolean
        get() = prefs.contains(KEY_ENCRYPTED_API_KEY) && prefs.contains(KEY_API_KEY_IV)

    val isRemoteRewriteConfigured: Boolean
        get() = rewriteEndpoint.isNotBlank() && hasApiKey

    fun saveApiKey(value: String): Boolean = saveEncryptedKey(
        value = value,
        ciphertextKey = KEY_ENCRYPTED_API_KEY,
        ivKey = KEY_API_KEY_IV,
        keystoreAlias = KEYSTORE_PORTAL_ALIAS,
        onClear = { clearApiKey() },
    )

    private fun saveEncryptedKey(
        value: String,
        ciphertextKey: String,
        ivKey: String,
        keystoreAlias: String,
        onClear: (() -> Unit)? = null,
    ): Boolean {
        val key = value.trim()
        if (key.isEmpty()) {
            if (onClear != null) onClear() else {
                prefs.edit().remove(ciphertextKey).remove(ivKey).apply()
            }
            return true
        }
        return runCatching {
            val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey(keystoreAlias))
            val encrypted = cipher.doFinal(key.toByteArray(Charsets.UTF_8))
            prefs.edit()
                .putString(ciphertextKey, Base64.encodeToString(encrypted, Base64.NO_WRAP))
                .putString(ivKey, Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
                .apply()
        }.isSuccess
    }

    fun apiKey(): String? = readEncryptedKey(
        ciphertextKey = KEY_ENCRYPTED_API_KEY,
        ivKey = KEY_API_KEY_IV,
        keystoreAlias = KEYSTORE_PORTAL_ALIAS,
    )

    private fun readEncryptedKey(
        ciphertextKey: String,
        ivKey: String,
        keystoreAlias: String,
    ): String? {
        val encrypted = prefs.getString(ciphertextKey, null) ?: return null
        val storedIv = prefs.getString(ivKey, null) ?: return null
        return runCatching {
            val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                secretKey(keystoreAlias),
                GCMParameterSpec(GCM_TAG_BITS, Base64.decode(storedIv, Base64.NO_WRAP)),
            )
            cipher.doFinal(Base64.decode(encrypted, Base64.NO_WRAP)).toString(Charsets.UTF_8)
        }.getOrNull()
    }

    fun clearApiKey() {
        prefs.edit().remove(KEY_ENCRYPTED_API_KEY).remove(KEY_API_KEY_IV).apply()
    }

    val hasGeminiApiKey: Boolean
        get() = prefs.contains(KEY_GEMINI_ENCRYPTED_API_KEY) &&
            prefs.contains(KEY_GEMINI_API_KEY_IV)

    var geminiModel: String
        get() = GeminiAudioTtsClient.normalizeTtsModel(
            prefs.getString(KEY_GEMINI_MODEL, GeminiAudioTtsClient.DEFAULT_TTS_MODEL).orEmpty(),
        )
        set(value) = prefs.edit()
            .putString(KEY_GEMINI_MODEL, GeminiAudioTtsClient.normalizeTtsModel(value))
            .apply()

    var geminiVoice: String
        get() {
            val raw = prefs.getString(KEY_GEMINI_VOICE, GeminiAudioTtsClient.DEFAULT_VOICE)
                .orEmpty()
                .trim()
            if (raw.isEmpty()) return GeminiAudioTtsClient.DEFAULT_VOICE
            // Preserve random sentinels; fixed names stay as stored.
            return raw
        }
        set(value) {
            val trimmed = value.trim().ifBlank { GeminiAudioTtsClient.DEFAULT_VOICE }
            prefs.edit().putString(KEY_GEMINI_VOICE, trimmed).apply()
        }

    var geminiLanguageCode: String
        get() = prefs.getString(KEY_GEMINI_LANGUAGE, GeminiAudioTtsClient.DEFAULT_LANGUAGE)
            .orEmpty()
            .ifBlank { GeminiAudioTtsClient.DEFAULT_LANGUAGE }
        set(value) = prefs.edit()
            .putString(
                KEY_GEMINI_LANGUAGE,
                value.trim().ifBlank { GeminiAudioTtsClient.DEFAULT_LANGUAGE },
            )
            .apply()

    var promptTemplate: String
        get() = prefs.getString(KEY_PROMPT_TEMPLATE, PromptTemplateDefaults.TEMPLATE)
            .orEmpty()
            .ifBlank { PromptTemplateDefaults.TEMPLATE }
        set(value) = prefs.edit().putString(KEY_PROMPT_TEMPLATE, value).apply()

    var activePromptTemplateId: String
        get() {
            ensureTemplateLibrarySeeded()
            return prefs.getString(KEY_ACTIVE_PROMPT_TEMPLATE_ID, AudioPromptPresets.ALL.first().id)
                .orEmpty()
                .ifBlank { AudioPromptPresets.ALL.first().id }
        }
        set(value) = prefs.edit().putString(KEY_ACTIVE_PROMPT_TEMPLATE_ID, value).apply()

    fun listTemplates(): List<PromptTemplateEntry> {
        ensureTemplateLibrarySeeded()
        return PromptTemplateLibraryCodec.decode(prefs.getString(KEY_PROMPT_TEMPLATES_JSON, null))
    }

    fun selectTemplate(id: String): Boolean {
        val entry = listTemplates().firstOrNull { it.id == id } ?: return false
        activePromptTemplateId = entry.id
        promptTemplate = entry.body
        return true
    }

    /** Updates active body in prefs + library entry (keeps name). */
    fun updateActiveTemplateBody(body: String) {
        promptTemplate = body
        val id = activePromptTemplateId
        val list = listTemplates().toMutableList()
        val idx = list.indexOfFirst { it.id == id }
        if (idx >= 0) {
            list[idx] = list[idx].copy(body = body, updatedAtMs = System.currentTimeMillis())
            writeTemplates(list)
        }
    }

    fun saveAsTemplate(name: String, body: String): PromptTemplateEntry? {
        val label = name.trim().ifBlank { return null }
        val text = body.trim().ifBlank { return null }
        val list = listTemplates().toMutableList()
        if (list.size >= PromptTemplateLibraryCodec.MAX_TEMPLATES) return null
        val entry = PromptTemplateEntry(
            id = "user_${UUID.randomUUID().toString().take(8)}",
            name = label.take(48),
            body = text,
            updatedAtMs = System.currentTimeMillis(),
            builtin = false,
        )
        list += entry
        writeTemplates(list)
        activePromptTemplateId = entry.id
        promptTemplate = entry.body
        return entry
    }

    fun saveActiveTemplateName(name: String): Boolean {
        val label = name.trim().ifBlank { return false }
        val list = listTemplates().toMutableList()
        val idx = list.indexOfFirst { it.id == activePromptTemplateId }
        if (idx < 0) return false
        list[idx] = list[idx].copy(
            name = label.take(48),
            body = promptTemplate,
            updatedAtMs = System.currentTimeMillis(),
        )
        writeTemplates(list)
        return true
    }

    fun deleteTemplate(id: String): Boolean {
        val list = listTemplates().toMutableList()
        if (list.size <= 1) return false
        val removed = list.removeAll { it.id == id }
        if (!removed) return false
        writeTemplates(list)
        if (activePromptTemplateId == id) {
            val next = list.first()
            activePromptTemplateId = next.id
            promptTemplate = next.body
        }
        return true
    }

    private fun ensureTemplateLibrarySeeded() {
        val raw = prefs.getString(KEY_PROMPT_TEMPLATES_JSON, null)
        if (!raw.isNullOrBlank() && raw != "[]") return
        val seed = PromptTemplateLibraryCodec.seedFromPresets()
        writeTemplates(seed)
        val currentBody = prefs.getString(KEY_PROMPT_TEMPLATE, null)
        val match = seed.firstOrNull { it.body == currentBody } ?: seed.first()
        prefs.edit()
            .putString(KEY_ACTIVE_PROMPT_TEMPLATE_ID, match.id)
            .putString(KEY_PROMPT_TEMPLATE, match.body)
            .apply()
    }

    private fun writeTemplates(entries: List<PromptTemplateEntry>) {
        prefs.edit()
            .putString(KEY_PROMPT_TEMPLATES_JSON, PromptTemplateLibraryCodec.encode(entries))
            .apply()
    }

    fun saveGeminiApiKey(value: String): Boolean = saveEncryptedKey(
        value = value,
        ciphertextKey = KEY_GEMINI_ENCRYPTED_API_KEY,
        ivKey = KEY_GEMINI_API_KEY_IV,
        keystoreAlias = KEYSTORE_GEMINI_ALIAS,
        onClear = {
            prefs.edit()
                .remove(KEY_GEMINI_ENCRYPTED_API_KEY)
                .remove(KEY_GEMINI_API_KEY_IV)
                .apply()
        },
    )

    fun geminiApiKey(): String? = readEncryptedKey(
        ciphertextKey = KEY_GEMINI_ENCRYPTED_API_KEY,
        ivKey = KEY_GEMINI_API_KEY_IV,
        keystoreAlias = KEYSTORE_GEMINI_ALIAS,
    )

    fun cascadeSpeechConfig(): CascadeSpeechConfig = CascadeSpeechConfig(
        geminiApiKey = geminiApiKey(),
        geminiModel = geminiModel,
        geminiVoice = geminiVoice,
        geminiLanguageCode = geminiLanguageCode,
        promptTemplate = promptTemplate,
        portalEndpoint = rewriteEndpoint,
        portalApiKey = apiKey(),
    )

    var quietStartMinutes: Int
        get() = prefs.getInt(KEY_QUIET_START, 22 * 60)
        set(value) = prefs.edit().putInt(KEY_QUIET_START, value).apply()

    var quietEndMinutes: Int
        get() = prefs.getInt(KEY_QUIET_END, 7 * 60)
        set(value) = prefs.edit().putInt(KEY_QUIET_END, value).apply()

    /** Default off for new installs and key-absent prefs (Media3 kept; toggle in settings). */
    var backgroundVideoEnabled: Boolean
        get() = prefs.getBoolean(KEY_BACKGROUND_VIDEO, false)
        set(value) = prefs.edit().putBoolean(KEY_BACKGROUND_VIDEO, value).apply()

    var backgroundVideoOpacity: Float
        get() = prefs.getFloat(KEY_BACKGROUND_OPACITY, 0.80f).coerceIn(0.4f, 1f)
        set(value) = prefs.edit()
            .putFloat(KEY_BACKGROUND_OPACITY, value.coerceIn(0.4f, 1f))
            .apply()

    /**
     * User preference for House HOME vs City HOME.
     * Effective house mode = [buzz.delena.forgecity.house.HouseFeatureFlags.use3dHouse] && this.
     * Default true matches the Wave-1 house surface when the compile flag is on.
     */
    var houseHomeEnabled: Boolean
        get() = prefs.getBoolean(KEY_HOUSE_HOME_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_HOUSE_HOME_ENABLED, value).apply()

    var launcherChromeVisible: Boolean
        get() = prefs.getBoolean(KEY_LAUNCHER_CHROME_VISIBLE, LauncherChromeDefaults.VISIBLE)
        set(value) = prefs.edit().putBoolean(KEY_LAUNCHER_CHROME_VISIBLE, value).apply()

    /**
     * City Assistant settings sheet (independent of search / dock).
     * City-first default: settings sheet is closed on first run (key absent).
     */
    var assistantPanelVisible: Boolean
        get() = prefs.getBoolean(KEY_ASSISTANT_PANEL_VISIBLE, false)
        set(value) = prefs.edit().putBoolean(KEY_ASSISTANT_PANEL_VISIBLE, value).apply()

    /** City-first default: search hidden on first run (key absent). */
    var searchBarVisible: Boolean
        get() = prefs.getBoolean(KEY_SEARCH_BAR_VISIBLE, false)
        set(value) = prefs.edit().putBoolean(KEY_SEARCH_BAR_VISIBLE, value).apply()

    /** City-first default: favorites dock on for first run (key absent). */
    var dockPanelVisible: Boolean
        get() = if (prefs.contains(KEY_DOCK_PANEL_VISIBLE)) {
            prefs.getBoolean(KEY_DOCK_PANEL_VISIBLE, true)
        } else {
            // Prefer true; fall back to legacy tools key if present.
            prefs.getBoolean(KEY_ASSISTANT_TOOLS_VISIBLE, true)
        }
        set(value) = prefs.edit().putBoolean(KEY_DOCK_PANEL_VISIBLE, value).apply()

    var speechTestText: String
        get() = prefs.getString(KEY_SPEECH_TEST_TEXT, SpeechTestDefaults.TEXT)
            .orEmpty()
            .ifBlank { SpeechTestDefaults.TEXT }
        set(value) = prefs.edit()
            .putString(KEY_SPEECH_TEST_TEXT, value.trim().ifBlank { SpeechTestDefaults.TEXT })
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

    private fun secretKey(alias: String = KEYSTORE_PORTAL_ALIAS): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        return keyStore.getKey(alias, null) as? SecretKey
            ?: error("Assistant API key is unavailable")
    }

    private fun getOrCreateSecretKey(alias: String): SecretKey {
        runCatching { secretKey(alias) }.getOrNull()?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build(),
        )
        return generator.generateKey()
    }

    companion object {
        private const val PREFS = "forgecity_assistant"
        private const val KEY_ASSISTANT = "assistant_enabled"
        private const val KEY_TTS = "tts_enabled"
        private const val KEY_REMOTE_REWRITE = "remote_rewrite_enabled"
        private const val KEY_SPEECH_MODE = "speech_mode"
        private const val KEY_REWRITE_ENDPOINT = "rewrite_endpoint"
        private const val KEY_ENCRYPTED_API_KEY = "rewrite_api_key_ciphertext"
        private const val KEY_API_KEY_IV = "rewrite_api_key_iv"
        private const val KEY_GEMINI_ENCRYPTED_API_KEY = "gemini_api_key_ciphertext"
        private const val KEY_GEMINI_API_KEY_IV = "gemini_api_key_iv"
        private const val KEY_GEMINI_MODEL = "gemini_model"
        private const val KEY_GEMINI_VOICE = "gemini_voice"
        private const val KEY_GEMINI_LANGUAGE = "gemini_language"
        private const val KEY_PROMPT_TEMPLATE = "prompt_template"
        private const val KEY_PROMPT_TEMPLATES_JSON = "prompt_templates_json"
        private const val KEY_ACTIVE_PROMPT_TEMPLATE_ID = "active_prompt_template_id"
        private const val KEY_ALLOW = "allowed_packages"
        private const val KEY_QUIET_START = "quiet_start"
        private const val KEY_QUIET_END = "quiet_end"
        private const val KEY_BACKGROUND_VIDEO = "background_video_enabled"
        private const val KEY_BACKGROUND_OPACITY = "background_video_opacity"
        private const val KEY_HOUSE_HOME_ENABLED = "house_home_enabled"
        private const val KEY_LAUNCHER_CHROME_VISIBLE = "launcher_chrome_visible"
        private const val KEY_ASSISTANT_TOOLS_VISIBLE = "assistant_tools_visible"
        private const val KEY_ASSISTANT_PANEL_VISIBLE = "assistant_panel_visible"
        private const val KEY_SEARCH_BAR_VISIBLE = "search_bar_visible"
        private const val KEY_DOCK_PANEL_VISIBLE = "dock_panel_visible"
        private const val KEY_SPEECH_TEST_TEXT = "speech_test_text"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEYSTORE_PORTAL_ALIAS = "forgecity_assistant_api_key"
        private const val KEYSTORE_GEMINI_ALIAS = "forgecity_gemini_api_key"
        private const val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_BITS = 128
    }
}
