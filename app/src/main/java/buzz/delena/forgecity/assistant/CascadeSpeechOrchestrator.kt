package buzz.delena.forgecity.assistant

import buzz.delena.forgecity.assistant.gemini.GeminiAudioResult
import buzz.delena.forgecity.assistant.gemini.GeminiAudioTtsClient
import buzz.delena.forgecity.assistant.gemini.GeminiVoiceResolver
import buzz.delena.forgecity.assistant.gemini.PromptModeValidator
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
    val geminiVoice: String,
    val geminiLanguageCode: String,
    val promptTemplate: String,
    val portalEndpoint: String,
    val portalApiKey: String?,
)

class CascadeSpeechOrchestrator(
    private val geminiAudioClient: GeminiAudioTtsClient = GeminiAudioTtsClient(),
    private val portalClient: AgentPortalRewriteClient = AgentPortalRewriteClient(),
    private val tts: AssistantTtsEngine,
) : AutoCloseable {

    fun run(
        input: CascadeSpeechInput,
        config: CascadeSpeechConfig,
        onStatus: ((String) -> Unit)? = null,
    ) {
        if (tryGeminiAudio(input, config, onStatus, failMessagePrefix = "trying Portal…")) return
        tryPortalThenDirect(input, config, onStatus)
    }

    /** Gemini native audio only: no Portal / device fallback (fail closed). */
    fun runGeminiOnly(
        input: CascadeSpeechInput,
        config: CascadeSpeechConfig,
        onStatus: ((String) -> Unit)? = null,
    ) {
        if (tryGeminiAudio(input, config, onStatus, failMessagePrefix = "stopped")) return
        onStatus?.invoke("GEMINI AUDIO failed closed")
    }

    private fun tryGeminiAudio(
        input: CascadeSpeechInput,
        config: CascadeSpeechConfig,
        onStatus: ((String) -> Unit)?,
        failMessagePrefix: String,
    ): Boolean {
        val promptCheck = PromptModeValidator.validate(
            mode = AssistantSpeechMode.GEMINI_TAMIL,
            template = config.promptTemplate,
        )
        if (!promptCheck.ok) {
            ForgeCityTtsDiagnostics.warn("cascade_skip", "tier=gemini_audio reason=prompt_invalid")
            onStatus?.invoke("${promptCheck.message}; $failMessagePrefix")
            return false
        }
        val prompt = PromptTemplateFormatter.format(
            template = config.promptTemplate,
            appLabel = input.appLabel,
            title = input.title,
            text = input.body,
        )
        val geminiKey = config.geminiApiKey?.takeIf { it.isNotBlank() }
        if (geminiKey == null) {
            ForgeCityTtsDiagnostics.info("cascade_skip", "tier=gemini_audio reason=no_key")
            onStatus?.invoke("Gemini key missing; $failMessagePrefix")
            return false
        }
        ForgeCityTtsDiagnostics.info("cascade_try", "tier=gemini_audio")
        val resolvedVoice = GeminiVoiceResolver.resolve(config.geminiVoice)
        ForgeCityTtsDiagnostics.info("gemini_voice_resolved", "voice=$resolvedVoice")
        return when (
            val result = geminiAudioClient.synthesize(
                apiKey = geminiKey,
                model = config.geminiModel,
                prompt = prompt,
                voice = resolvedVoice,
                languageCode = config.geminiLanguageCode,
            )
        ) {
            is GeminiAudioResult.Success -> {
                playGeminiAudio(result, onStatus)
                true
            }
            is GeminiAudioResult.Unauthorized -> {
                onStatus?.invoke("Gemini auth failed (bad key); $failMessagePrefix")
                false
            }
            is GeminiAudioResult.ModelUnavailable -> {
                onStatus?.invoke("Gemini TTS model unavailable; $failMessagePrefix")
                false
            }
            is GeminiAudioResult.Timeout -> {
                onStatus?.invoke("Gemini audio timeout; $failMessagePrefix")
                false
            }
            is GeminiAudioResult.Malformed -> {
                onStatus?.invoke("Gemini audio malformed; $failMessagePrefix")
                false
            }
            is GeminiAudioResult.Unavailable -> {
                onStatus?.invoke("Gemini audio unavailable; $failMessagePrefix")
                false
            }
        }
    }

    private fun playGeminiAudio(
        result: GeminiAudioResult.Success,
        onStatus: ((String) -> Unit)?,
    ) {
        tts.playPcm(result.pcm, result.sampleRateHz) { speakResult ->
            ForgeCityTtsDiagnostics.info(
                "cascade_gemini_audio_result",
                "result=$speakResult rateHz=${result.sampleRateHz}",
            )
            onStatus?.invoke(
                if (speakResult == AssistantTtsEngine.SpeakResult.STARTED) {
                    "Gemini native audio started"
                } else {
                    "Gemini audio OK; playback $speakResult"
                },
            )
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
        geminiAudioClient.close()
        portalClient.close()
    }
}
