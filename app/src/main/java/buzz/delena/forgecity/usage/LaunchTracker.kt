package buzz.delena.forgecity.usage

import buzz.delena.forgecity.data.BuildingStatEntity
import buzz.delena.forgecity.data.CityDao

/**
 * Room-backed launch progression (Stream C: single source of truth for levels).
 * Replaces the earlier SharedPreferences store so levels persist and stay observable.
 */
class LaunchTracker(private val dao: CityDao) {

    /** Records a launch and returns the building's new level. */
    suspend fun recordLaunch(buildingId: String): Int {
        val current = dao.getBuildingStat(buildingId) ?: BuildingStatEntity(id = buildingId)
        val newCount = current.launchCount + 1
        val level = UsageXpCalculator.levelForLaunchCount(newCount)
        dao.upsertBuildingStat(current.copy(launchCount = newCount, level = level))
        return level
    }

    /** Snapshot of buildingId -> level for grid rendering. */
    suspend fun levels(): Map<String, Int> =
        dao.getBuildingStats().associate { it.id to it.level }
}
