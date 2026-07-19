package buzz.delena.forgecity.data

import buzz.delena.forgecity.city.CityResources
import buzz.delena.forgecity.city.CityState
import buzz.delena.forgecity.city.District
import buzz.delena.forgecity.story.StoryCatalog
import buzz.delena.forgecity.usage.UsageXpCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CityRepository(
    private val dao: CityDao,
) {
    fun observeState(): Flow<CityState> = dao.observeMeta().map { meta ->
        val safe = meta ?: CityMetaEntity()
        CityState(
            chapterId = safe.chapterId,
            chapterTitle = safe.chapterTitle,
            briefing = StoryCatalog.briefingFor(safe.chapterId),
            resources = CityResources(
                scrap = safe.scrap,
                power = safe.power,
                focus = safe.focus,
                goldDust = safe.goldDust,
            ),
            unlockedDistricts = unlockedForChapter(safe.chapterId),
        )
    }

    /** Insert defaults only when absent so resources/progress survive restarts. */
    suspend fun ensureSeeded() {
        dao.insertMetaIfAbsent(CityMetaEntity())
        StoryCatalog.seededQuests().forEach { dao.insertQuestIfAbsent(it) }
    }

    suspend fun applyUsageGains(gains: UsageXpCalculator.Gains) {
        if (gains.scrap == 0 && gains.power == 0 && gains.focus == 0 && gains.goldDust == 0) {
            return
        }
        ensureSeeded()
        dao.addResources(
            scrap = gains.scrap,
            power = gains.power,
            focus = gains.focus,
            goldDust = gains.goldDust,
        )
    }

    /** Debounce gate: only harvest after [minIntervalMs] since the last run. */
    suspend fun shouldHarvest(now: Long, minIntervalMs: Long): Boolean {
        val meta = dao.getMeta() ?: return true
        return now - meta.lastHarvestEpoch >= minIntervalMs
    }

    suspend fun markHarvested(now: Long) {
        ensureSeeded()
        dao.setLastHarvest(now)
    }

    private fun unlockedForChapter(chapterId: Int): Set<District> =
        District.entries.filter { it.unlockChapter <= chapterId }.toSet()
}
