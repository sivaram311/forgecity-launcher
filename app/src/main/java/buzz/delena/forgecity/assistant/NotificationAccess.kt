package buzz.delena.forgecity.assistant

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.text.TextUtils

object NotificationAccess {
    fun hasAccess(context: Context): Boolean {
        val flat = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners",
        ) ?: return false
        if (TextUtils.isEmpty(flat)) return false
        val cn = ComponentName(context, ForgeNotificationListenerService::class.java)
        return flat.split(':').any {
            ComponentName.unflattenFromString(it)?.flattenToString() == cn.flattenToString()
        }
    }

    fun settingsIntent(context: Context): Intent {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val detail = Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS).apply {
                putExtra(
                    Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME,
                    ComponentName(context, ForgeNotificationListenerService::class.java)
                        .flattenToString(),
                )
            }
            if (detail.resolveActivity(context.packageManager) != null) return detail
        }
        return Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
    }
}
