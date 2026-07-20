package buzz.delena.forgecity.assistant

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import buzz.delena.forgecity.assistant.rewrite.AgentPortalRewriteClient
import buzz.delena.forgecity.assistant.rewrite.NotificationRewritePipeline
import buzz.delena.forgecity.assistant.rewrite.RewriteRequest
import java.util.Calendar
import java.util.concurrent.Executors

class ForgeNotificationListenerService : NotificationListenerService() {
    private val settings by lazy { AssistantSettingsStore(this) }
    private val speechBudget by lazy { SpeechBudget(this) }
    private val dedupe = NotificationDedupe()
    private var tts: AssistantTtsEngine? = null
    private var rewritePipeline: NotificationRewritePipeline? = null
    private var cascadeOrchestrator: CascadeSpeechOrchestrator? = null
    private val cascadeExecutor = Executors.newSingleThreadExecutor { task ->
        Thread(task, "forgecity-cascade").apply { isDaemon = true }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        ForgeCityTtsDiagnostics.info("listener_connected")
        val engine = AssistantTtsEngine(this)
        tts = engine
        rewritePipeline = NotificationRewritePipeline(
            client = AgentPortalRewriteClient(),
            tts = engine,
            canProceed = ::canUseRemoteSpeech,
            endpoint = { settings.rewriteEndpoint },
            apiKey = settings::apiKey,
        )
        cascadeOrchestrator = CascadeSpeechOrchestrator(tts = engine)
    }

    override fun onListenerDisconnected() {
        closeSpeechPipeline()
        super.onListenerDisconnected()
    }

    override fun onDestroy() {
        closeSpeechPipeline()
        super.onDestroy()
    }

    private fun closeSpeechPipeline() {
        rewritePipeline?.close()
        rewritePipeline = null
        cascadeOrchestrator?.close()
        cascadeOrchestrator = null
        cascadeExecutor.shutdownNow()
        tts?.shutdown()
        tts = null
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val notification = sbn ?: return
        ForgeCityTtsDiagnostics.info("notification_received", "package=${notification.packageName}")
        if (!settings.assistantEnabled) {
            ForgeCityTtsDiagnostics.warn("notification_skipped", "reason=assistant_disabled")
            return
        }
        if (!speechBudget.allowsSpeech) {
            ForgeCityTtsDiagnostics.warn("notification_skipped", "reason=speech_budget")
            return
        }
        val now = Calendar.getInstance()
        val minutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        if (QuietHours.isQuiet(minutes, settings.quietStartMinutes, settings.quietEndMinutes)) {
            ForgeCityTtsDiagnostics.warn("notification_skipped", "reason=quiet_hours")
            return
        }
        if (!dedupe.shouldProcess(notification.key)) {
            ForgeCityTtsDiagnostics.warn("notification_skipped", "reason=dedupe")
            return
        }

        val n = notification.notification
        val extras = n.extras
        val title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: extras?.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
        val flags = n.flags
        val ongoing = flags and Notification.FLAG_ONGOING_EVENT != 0
        val groupSummary = flags and Notification.FLAG_GROUP_SUMMARY != 0
        val fgs = flags and Notification.FLAG_FOREGROUND_SERVICE != 0
        if (!NotificationSpeechFilter.shouldSpeak(
                packageName = notification.packageName,
                ownPackage = packageName,
                allowedPackages = settings.allowedPackages(),
                title = title,
                text = text,
                isOngoing = ongoing,
                isGroupSummary = groupSummary,
                isForegroundService = fgs,
            )
        ) {
            ForgeCityTtsDiagnostics.warn(
                "notification_skipped",
                "reason=filter_or_allowlist package=${notification.packageName}",
            )
            return
        }

        val label = runCatching {
            packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(notification.packageName, 0),
            ).toString()
        }.getOrDefault(notification.packageName)

        val event = AssistantUiEvent(
            packageName = notification.packageName,
            appLabel = label,
            title = title.orEmpty().take(80),
            shortText = text.orEmpty().take(120),
            notificationKey = notification.key,
            contentIntent = n.contentIntent,
        )
        AssistantEventBridge.emit(event)

        val route = NotificationSpeechRoute.resolve(
                mode = settings.speechMode,
                portalConfigured = settings.isRemoteRewriteConfigured,
                geminiConfigured = settings.hasGeminiApiKey,
            )
        ForgeCityTtsDiagnostics.info("notification_route", "mode=${settings.speechMode} route=$route")
        when (route) {
            NotificationSpeechRoute.NONE -> Unit
            NotificationSpeechRoute.DIRECT -> {
                tts?.speakDirect(NotificationSpeechFilter.spokenLine(label, title, text))
            }
            NotificationSpeechRoute.AGENT_PORTAL_TAMIL -> {
                rewritePipeline?.enqueue(
                    RewriteRequest(
                        notificationKey = notification.key,
                        appLabel = label,
                        title = title.orEmpty(),
                        body = text.orEmpty(),
                    ),
                )
            }
            NotificationSpeechRoute.GEMINI_TAMIL -> {
                cascadeExecutor.execute {
                    if (!canUseGeminiSpeech()) return@execute
                    cascadeOrchestrator?.runGeminiOnly(
                        CascadeSpeechInput(
                            notificationKey = notification.key,
                            appLabel = label,
                            title = title.orEmpty(),
                            body = text.orEmpty(),
                        ),
                        settings.cascadeSpeechConfig(),
                    )
                }
            }
            NotificationSpeechRoute.SMART_CASCADE -> {
                cascadeExecutor.execute {
                    if (!canUseCascadeSpeech()) return@execute
                    cascadeOrchestrator?.run(
                        CascadeSpeechInput(
                            notificationKey = notification.key,
                            appLabel = label,
                            title = title.orEmpty(),
                            body = text.orEmpty(),
                        ),
                        settings.cascadeSpeechConfig(),
                    )
                }
            }
        }
    }

    private fun canUseGeminiSpeech(): Boolean {
        if (!settings.assistantEnabled ||
            settings.speechMode != AssistantSpeechMode.GEMINI_TAMIL ||
            !settings.hasGeminiApiKey ||
            !speechBudget.allowsSpeech
        ) {
            return false
        }
        val now = Calendar.getInstance()
        val minutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        return !QuietHours.isQuiet(
            minutes,
            settings.quietStartMinutes,
            settings.quietEndMinutes,
        )
    }

    private fun canUseCascadeSpeech(): Boolean {
        if (!settings.assistantEnabled ||
            settings.speechMode != AssistantSpeechMode.SMART_CASCADE ||
            !speechBudget.allowsSpeech
        ) {
            return false
        }
        val now = Calendar.getInstance()
        val minutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        return !QuietHours.isQuiet(
            minutes,
            settings.quietStartMinutes,
            settings.quietEndMinutes,
        )
    }

    private fun canUseRemoteSpeech(): Boolean {
        if (!settings.assistantEnabled ||
            settings.speechMode != AssistantSpeechMode.AGENT_PORTAL_TAMIL ||
            !settings.isRemoteRewriteConfigured ||
            !speechBudget.allowsSpeech
        ) {
            return false
        }
        val now = Calendar.getInstance()
        val minutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        return !QuietHours.isQuiet(
            minutes,
            settings.quietStartMinutes,
            settings.quietEndMinutes,
        )
    }

    companion object {
        @Volatile
        var pendingOpenKey: String? = null
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        sbn?.key?.let { rewritePipeline?.cancel(it) }
    }
}
