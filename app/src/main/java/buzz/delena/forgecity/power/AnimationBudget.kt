package buzz.delena.forgecity.power

import android.content.Context
import android.os.PowerManager

/**
 * Gates heavy ambient animation for Realme mid-range battery/thermal budget.
 * Stream B (Android Systems) owns this API; UI only reads [allowsAmbient].
 */
class AnimationBudget(context: Context) {
    private val powerManager =
        context.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager

    val allowsAmbient: Boolean
        get() = !powerManager.isPowerSaveMode && !powerManager.isDeviceIdleMode
}
