package buzz.delena.forgecity.usage

import android.content.Context

class LaunchTracker(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun recordLaunch(buildingId: String) {
        val key = KEY_PREFIX + buildingId
        prefs.edit().putInt(key, prefs.getInt(key, 0) + 1).apply()
    }

    fun launchCount(buildingId: String): Int =
        prefs.getInt(KEY_PREFIX + buildingId, 0)

    fun levelFor(buildingId: String): Int =
        UsageXpCalculator.levelForLaunchCount(launchCount(buildingId))

    companion object {
        private const val PREFS = "forgecity_launches"
        private const val KEY_PREFIX = "launch:"
    }
}
