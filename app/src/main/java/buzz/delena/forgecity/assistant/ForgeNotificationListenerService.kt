package buzz.delena.forgecity.assistant

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import buzz.delena.forgecity.assistant.rewrite.AgentPortalRewriteClient
import buzz.delena.forgecity.assistant.rewrite.NotificationRewritePipeline
import buzz.delena.forgecity.assistant.rewrite.RewriteRequest
import java.util.Calendar

class ForgeNotificationListenerService : NotificationListenerService() {
    private val settings by lazy { AssistantSettingsStore(this) }
    private val speechBudget by lazy { SpeechBudget(this) }
    private val dedupe = NotificationDedupe()
    private var tts: AssistantTtsEngine? = null
    private var rewritePipeline: NotificationRewritePipeline? = null

    override fun onListenerConnected() {
        super.onListenerConnected()
        val engine = AssistantTtsEngine(this)
        tts = engine
        rewritePipeline = NotificationRewritePipeline(
            client = AgentPortalRewriteClient(),
            tts = engine,
            canProceed = ::canUseRemoteSpeech,
            endpoint = { settings.rewriteEndpoint },
            apiKey = settings::apiKey,
        )
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
        tts?.shutdown()
        tts = null
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val notification = sbn ?: return
        if (!settings.assistantEnabled) return
        if (!speechBudget.allowsSpeech) return
        val now = Calendar.getInstance()
        val minutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        if (QuietHours.isQuiet(minutes, settings.quietStartMinutes, settings.quietEndMinutes)) {
            return
        }
        if (!dedupe.shouldProcess(notification.key)) return

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

        when (
            NotificationSpeechRoute.resolve(
                mode = settings.speechMode,
                portalConfigured = settings.isRemoteRewriteConfigured,
            )
        ) {
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
        }
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
