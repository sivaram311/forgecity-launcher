package buzz.delena.forgecity.usage

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings
import buzz.delena.forgecity.city.District
import buzz.delena.forgecity.city.DistrictClassifier

class UsageStatsHarvester(private val context: Context) {
    fun hasUsageAccess(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName,
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun usageAccessSettingsIntent(): Intent =
        Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)

    fun harvestLastHours(hours: Long = 24): UsageXpCalculator.Gains {
        if (!hasUsageAccess()) {
            return UsageXpCalculator.Gains(0, 0, 0, 0)
        }
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val end = System.currentTimeMillis()
        val start = end - hours * 60L * 60L * 1000L
        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end).orEmpty()
        var totalMs = 0L
        var prodMs = 0L
        var finMs = 0L
        stats.forEach { stat ->
            val ms = stat.totalTimeInForeground
            if (ms <= 0) return@forEach
            totalMs += ms
            when (DistrictClassifier.classify(stat.packageName, stat.packageName)) {
                District.FORGE, District.GARDEN, District.ARCHIVE -> prodMs += ms
                District.VAULT -> finMs += ms
                else -> Unit
            }
        }
        return UsageXpCalculator.fromForegroundMinutes(
            totalMinutes = totalMs / 60_000L,
            productiveMinutes = prodMs / 60_000L,
            financeMinutes = finMs / 60_000L,
            organizedAppCount = stats.count { it.totalTimeInForeground > 0 },
        )
    }
}
