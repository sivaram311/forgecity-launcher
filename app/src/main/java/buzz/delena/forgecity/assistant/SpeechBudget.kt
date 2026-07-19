package buzz.delena.forgecity.assistant

import android.content.Context
import android.os.PowerManager

/** Speech is stricter than ambient: requires interactive screen. */
class SpeechBudget(context: Context) {
    private val powerManager =
        context.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager

    val allowsSpeech: Boolean
        get() = powerManager.isInteractive &&
            !powerManager.isPowerSaveMode &&
            !powerManager.isDeviceIdleMode
}
