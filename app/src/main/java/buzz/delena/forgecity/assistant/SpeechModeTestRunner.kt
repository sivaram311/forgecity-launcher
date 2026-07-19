package buzz.delena.forgecity.assistant

import android.content.Context
import buzz.delena.forgecity.assistant.rewrite.AgentPortalRewriteClient
import buzz.delena.forgecity.assistant.rewrite.RewriteRequest
import buzz.delena.forgecity.assistant.rewrite.RewriteResult
import java.util.concurrent.Executors

class SpeechModeTestRunner(context: Context) : AutoCloseable {
    private val tts = AssistantTtsEngine(context)
    private val executor = Executors.newSingleThreadExecutor { task ->
        Thread(task, "forgecity-tts-test").apply { isDaemon = true }
    }

    fun run(
        mode: AssistantSpeechMode,
        endpoint: String,
        apiKey: String?,
        onStatus: (String) -> Unit,
    ) {
        ForgeCityTtsDiagnostics.info("test_start", "mode=$mode")
        when (mode) {
            AssistantSpeechMode.OFF -> {
                ForgeCityTtsDiagnostics.warn("test_blocked", "reason=mode_off")
                onStatus("OFF: select DIRECT or PORTAL தமிழ்")
            }
            AssistantSpeechMode.DIRECT_TTS -> {
                onStatus("Testing device TTS…")
                tts.speakDirect("ForgeCity direct speech test.") { result ->
                    ForgeCityTtsDiagnostics.info("test_direct_result", "result=$result")
                    onStatus(
                        if (result == AssistantTtsEngine.SpeakResult.STARTED) {
                            "DIRECT test started"
                        } else {
                            "DIRECT failed: $result"
                        },
                    )
                }
            }
            AssistantSpeechMode.AGENT_PORTAL_TAMIL -> {
                if (endpoint.isBlank() || apiKey.isNullOrBlank()) {
                    ForgeCityTtsDiagnostics.warn("test_portal_blocked", "reason=config_missing")
                    onStatus("PORTAL failed: endpoint/key missing")
                    return
                }
                onStatus("Testing PROD rewrite + Tamil TTS…")
                executor.execute {
                    val result = AgentPortalRewriteClient().use { client ->
                        client.rewrite(
                            endpoint,
                            apiKey,
                            RewriteRequest(
                                notificationKey = "manual-tts-test",
                                appLabel = "ForgeCity",
                                title = "Speech test",
                                body = "ForgeCity notification speech is working.",
                            ),
                        )
                    }
                    ForgeCityTtsDiagnostics.info(
                        "test_portal_rewrite_result",
                        "result=${result::class.simpleName}",
                    )
                    if (result !is RewriteResult.Success) {
                        onStatus("PORTAL failed: ${result::class.simpleName}")
                        return@execute
                    }
                    tts.speakTamil(result.tamilText) { speakResult ->
                        ForgeCityTtsDiagnostics.info(
                            "test_portal_tts_result",
                            "result=$speakResult",
                        )
                        onStatus(
                            if (speakResult == AssistantTtsEngine.SpeakResult.STARTED) {
                                "PORTAL Tamil test started"
                            } else {
                                "PORTAL rewrite OK; Tamil TTS $speakResult"
                            },
                        )
                    }
                }
            }
        }
    }

    override fun close() {
        executor.shutdownNow()
        tts.shutdown()
    }
}
