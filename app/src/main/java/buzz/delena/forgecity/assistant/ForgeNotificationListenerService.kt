package buzz.delena.forgecity.assistant

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import java.util.Calendar

class ForgeNotificationListenerService : NotificationListenerService() {
    private val settings by lazy { AssistantSettingsStore(this) }
    private val speechBudget by lazy { SpeechBudget(this) }
    private val dedupe = NotificationDedupe()
    private var tts: AssistantTtsEngine? = null

    override fun onListenerConnected() {
        super.onListenerConnected()
        tts = AssistantTtsEngine(this)
    }

    override fun onListenerDisconnected() {
        tts?.shutdown()
        tts = null
        super.onListenerDisconnected()
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
        )
        AssistantEventBridge.emit(event)

        if (settings.ttsEnabled) {
            val line = NotificationSpeechFilter.spokenLine(label, title, text)
            tts?.speak(line)
        }
    }

    companion object {
        @Volatile
        var pendingOpenKey: String? = null
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // no-op — ephemeral UI only
    }
}
