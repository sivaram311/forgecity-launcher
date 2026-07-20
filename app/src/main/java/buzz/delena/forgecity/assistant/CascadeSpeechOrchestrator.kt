package buzz.delena.forgecity.assistant

import buzz.delena.forgecity.assistant.gemini.GeminiRewriteClient
import buzz.delena.forgecity.assistant.gemini.GeminiRewriteResult
import buzz.delena.forgecity.assistant.gemini.PromptTemplateFormatter
import buzz.delena.forgecity.assistant.rewrite.AgentPortalRewriteClient
import buzz.delena.forgecity.assistant.rewrite.RewriteRequest
import buzz.delena.forgecity.assistant.rewrite.RewriteResult

data class CascadeSpeechInput(
    val notificationKey: String,
    val appLabel: String,
    val title: String,
    val body: String,
)

data class CascadeSpeechConfig(
    val geminiApiKey: String?,
    val geminiModel: String,
    val promptTemplate: String,
    val portalEndpoint: String,
    val portalApiKey: String?,
)

class CascadeSpeechOrchestrator(
    private val geminiClient: GeminiRewriteClient = GeminiRewriteClient(),
    private val portalClient: AgentPortalRewriteClient = AgentPortalRewriteClient(),
    private val tts: AssistantTtsEngine,
) : AutoCloseable {

    fun run(
        input: CascadeSpeechInput,
        config: CascadeSpeechConfig,
        onStatus: ((String) -> Unit)? = null,
    ) {
        if (tryGemini(input, config, onStatus, failMessagePrefix = "trying Portal…")) return
        tryPortalThenDirect(input, config, onStatus)
    }

    /** Gemini-only path: no Portal / device fallback (fail closed). */
    fun runGeminiOnly(
        input: CascadeSpeechInput,
        config: CascadeSpeechConfig,
        onStatus: ((String) -> Unit)? = null,
    ) {
        if (tryGemini(input, config, onStatus, failMessagePrefix = "stopped")) return
        onStatus?.invoke("GEMINI failed closed")
    }

    private fun tryGemini(
        input: CascadeSpeechInput,
        config: CascadeSpeechConfig,
        onStatus: ((String) -> Unit)?,
        failMessagePrefix: String,
    ): Boolean {
        val prompt = PromptTemplateFormatter.format(
            template = config.promptTemplate,
            appLabel = input.appLabel,
            title = input.title,
            text = input.body,
        )
        val geminiKey = config.geminiApiKey?.takeIf { it.isNotBlank() }
        if (geminiKey == null) {
            ForgeCityTtsDiagnostics.info("cascade_skip", "tier=gemini reason=no_key")
            onStatus?.invoke("Gemini key missing; $failMessagePrefix")
            return false
        }
        ForgeCityTtsDiagnostics.info("cascade_try", "tier=gemini")
        return when (val result = geminiClient.rewrite(geminiKey, config.geminiModel, prompt)) {
            is GeminiRewriteResult.Success -> {
                speakTamil(result.tamilText, onStatus, "Gemini")
                true
            }
            is GeminiRewriteResult.Unauthorized -> {
                onStatus?.invoke("Gemini auth failed (bad key); $failMessagePrefix")
                false
            }
            is GeminiRewriteResult.ModelUnavailable -> {
                onStatus?.invoke("Gemini model unavailable; $failMessagePrefix")
                false
            }
            is GeminiRewriteResult.Timeout -> {
                onStatus?.invoke("Gemini timeout; $failMessagePrefix")
                false
            }
            is GeminiRewriteResult.Malformed -> {
                onStatus?.invoke("Gemini response not Tamil; $failMessagePrefix")
                false
            }
            is GeminiRewriteResult.Unavailable -> {
                onStatus?.invoke("Gemini unavailable; $failMessagePrefix")
                false
            }
        }
    }

    private fun tryPortalThenDirect(
        input: CascadeSpeechInput,
        config: CascadeSpeechConfig,
        onStatus: ((String) -> Unit)?,
    ) {
        val portalKey = config.portalApiKey?.takeIf { it.isNotBlank() }
        val portalEndpoint = config.portalEndpoint.takeIf { it.isNotBlank() }
        if (portalKey != null && portalEndpoint != null) {
            ForgeCityTtsDiagnostics.info("cascade_try", "tier=portal")
            val result = portalClient.rewrite(
                portalEndpoint,
                portalKey,
                RewriteRequest(
                    notificationKey = input.notificationKey,
                    appLabel = input.appLabel,
                    title = input.title,
                    body = input.body,
                ),
            )
            ForgeCityTtsDiagnostics.info(
                "cascade_portal_result",
                "result=${result::class.simpleName}",
            )
            if (result is RewriteResult.Success) {
                speakTamil(result.tamilText, onStatus, "Portal")
                return
            } else {
                onStatus?.invoke("Portal ${result::class.simpleName}; trying device TTS…")
            }
        } else {
            ForgeCityTtsDiagnostics.info("cascade_skip", "tier=portal reason=not_configured")
        }

        ForgeCityTtsDiagnostics.info("cascade_try", "tier=direct")
        val directLine = NotificationSpeechFilter.spokenLine(
            input.appLabel,
            input.title,
            input.body,
        )
        tts.speakDirect(directLine) { result ->
            ForgeCityTtsDiagnostics.info("cascade_direct_result", "result=$result")
            onStatus?.invoke(
                if (result == AssistantTtsEngine.SpeakResult.STARTED) {
                    "Device TTS started (fallback)"
                } else {
                    "All tiers failed: $result"
                },
            )
        }
    }

    private fun speakTamil(
        text: String,
        onStatus: ((String) -> Unit)?,
        label: String,
    ) {
        tts.speakTamil(text) { result ->
            ForgeCityTtsDiagnostics.info("cascade_tamil_result", "tier=$label result=$result")
            onStatus?.invoke(
                if (result == AssistantTtsEngine.SpeakResult.STARTED) {
                    "$label Tamil TTS started"
                } else {
                    "$label rewrite OK; Tamil TTS $result"
                },
            )
        }
    }

    override fun close() {
        geminiClient.close()
        portalClient.close()
    }
}
