package buzz.delena.forgecity.data

import buzz.delena.forgecity.city.CityResources
import buzz.delena.forgecity.city.CityState
import buzz.delena.forgecity.city.District
import buzz.delena.forgecity.story.StoryCatalog
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

    suspend fun ensureSeeded() {
        dao.upsertMeta(CityMetaEntity())
        StoryCatalog.starterQuests().forEach { dao.upsertQuest(it) }
    }

    private fun unlockedForChapter(chapterId: Int): Set<District> =
        District.entries.filter { it.unlockChapter <= chapterId }.toSet()
}
