package buzz.delena.forgecity.usage

import buzz.delena.forgecity.city.FavoritePolicy
import buzz.delena.forgecity.data.BuildingStatEntity
import buzz.delena.forgecity.data.CityDao

/**
 * Room-backed launch progression + favorites (Stream C SoT).
 */
class LaunchTracker(private val dao: CityDao) {

    suspend fun recordLaunch(buildingId: String): Int {
        val current = dao.getBuildingStat(buildingId) ?: BuildingStatEntity(id = buildingId)
        val newCount = current.launchCount + 1
        val level = UsageXpCalculator.levelForLaunchCount(newCount)
        dao.upsertBuildingStat(current.copy(launchCount = newCount, level = level))
        return level
    }

    suspend fun levels(): Map<String, Int> =
        dao.getBuildingStats().associate { it.id to it.level }

    suspend fun favorites(): Map<String, Boolean> =
        dao.getBuildingStats().associate { it.id to it.isFavorite }

    /**
     * @return true if the pin state changed (or unpinned); false if dock is full.
     */
    suspend fun toggleFavorite(buildingId: String): Boolean {
        val current = dao.getBuildingStat(buildingId) ?: BuildingStatEntity(id = buildingId)
        if (current.isFavorite) {
            dao.upsertBuildingStat(current.copy(isFavorite = false))
            return true
        }
        val count = dao.favoriteCount()
        if (!FavoritePolicy.canPin(count, currentlyFavorite = false)) {
            return false
        }
        dao.upsertBuildingStat(current.copy(isFavorite = true))
        return true
    }
}
