package buzz.delena.forgecity.usage

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import buzz.delena.forgecity.data.CityRepository
import buzz.delena.forgecity.data.ForgeCityDatabase
import java.util.concurrent.TimeUnit

class ResourceHarvestWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val harvester = UsageStatsHarvester(applicationContext)
        if (!harvester.hasUsageAccess()) return Result.success()
        val repo = CityRepository(ForgeCityDatabase.get(applicationContext).cityDao())
        val now = System.currentTimeMillis()
        if (!repo.shouldHarvest(now, MIN_INTERVAL_MS)) return Result.success()
        repo.applyUsageGains(harvester.harvestLastHours(24))
        repo.markHarvested(now)
        return Result.success()
    }

    companion object {
        private const val UNIQUE = "forgecity-resource-harvest"
        private const val MIN_INTERVAL_MS = 60L * 60L * 1000L

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<ResourceHarvestWorker>(6, TimeUnit.HOURS)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
