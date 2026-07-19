package buzz.delena.forgecity.story

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StoryCatalogTest {
    @Test
    fun seededQuestsHaveUniqueIds() {
        val ids = StoryCatalog.seededQuests().map { it.questId }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun seededQuestsCoverChaptersOneThroughThree() {
        val chapters = StoryCatalog.seededQuests().map { it.chapterId }.toSet()
        assertTrue(chapters.containsAll(setOf(1, 2, 3)))
    }

    @Test
    fun onlyChapterOneStartsActive() {
        val active = StoryCatalog.seededQuests().filter { it.status == "active" }
        assertTrue(active.all { it.chapterId == 1 })
        assertTrue(active.isNotEmpty())
    }
}
