package buzz.delena.forgecity.assistant

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Privacy-first assistant prefs. Never stores notification title/body.
 * Defaults: speech off, launcher chrome hidden, allowlist empty.
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

    fun saveApiKey(value: String): Boolean {
        val key = value.trim()
        if (key.isEmpty()) {
            clearApiKey()
            return true
        }
        return runCatching {
            val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
            val encrypted = cipher.doFinal(key.toByteArray(Charsets.UTF_8))
            prefs.edit()
                .putString(KEY_ENCRYPTED_API_KEY, Base64.encodeToString(encrypted, Base64.NO_WRAP))
                .putString(KEY_API_KEY_IV, Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
                .apply()
        }.isSuccess
    }

    fun apiKey(): String? {
        val encrypted = prefs.getString(KEY_ENCRYPTED_API_KEY, null) ?: return null
        val iv = prefs.getString(KEY_API_KEY_IV, null) ?: return null
        return runCatching {
            val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                secretKey(),
                GCMParameterSpec(GCM_TAG_BITS, Base64.decode(iv, Base64.NO_WRAP)),
            )
            cipher.doFinal(Base64.decode(encrypted, Base64.NO_WRAP)).toString(Charsets.UTF_8)
        }.getOrNull()
    }

    fun clearApiKey() {
        prefs.edit().remove(KEY_ENCRYPTED_API_KEY).remove(KEY_API_KEY_IV).apply()
    }

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

    var launcherChromeVisible: Boolean
        get() = prefs.getBoolean(KEY_LAUNCHER_CHROME_VISIBLE, LauncherChromeDefaults.VISIBLE)
        set(value) = prefs.edit().putBoolean(KEY_LAUNCHER_CHROME_VISIBLE, value).apply()

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

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        return keyStore.getKey(KEYSTORE_ALIAS, null) as? SecretKey
            ?: error("Assistant API key is unavailable")
    }

    private fun getOrCreateSecretKey(): SecretKey {
        runCatching { secretKey() }.getOrNull()?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
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
        private const val KEY_ALLOW = "allowed_packages"
        private const val KEY_QUIET_START = "quiet_start"
        private const val KEY_QUIET_END = "quiet_end"
        private const val KEY_BACKGROUND_VIDEO = "background_video_enabled"
        private const val KEY_BACKGROUND_OPACITY = "background_video_opacity"
        private const val KEY_LAUNCHER_CHROME_VISIBLE = "launcher_chrome_visible"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEYSTORE_ALIAS = "forgecity_assistant_api_key"
        private const val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_BITS = 128
    }
}
