package buzz.delena.forgecity.story

import buzz.delena.forgecity.data.StoryProgressEntity

object StoryCatalog {
    fun briefingFor(chapterId: Int): String = when (chapterId) {
        1 -> "Embers remain. Organize twenty apps into starter buildings and restore the first power grid."
        2 -> "Foundations rise. Complete focus blocks to unlock taller towers and new districts."
        3 -> "Specialize. Trading feeds Vault Empire; coding feeds Tech Spire."
        else -> "The city remembers your habits. Keep balance for the Symphony ending."
    }

    fun starterQuests(): List<StoryProgressEntity> = listOf(
        StoryProgressEntity(
            questId = "embers-organize-20",
            chapterId = 1,
            title = "Raise the first twenty buildings",
            status = "active",
            progress = 0,
            goal = 20,
        ),
        StoryProgressEntity(
            questId = "embers-power-grid",
            chapterId = 1,
            title = "Restore night lighting",
            status = "locked",
            progress = 0,
            goal = 1,
        ),
    )

    /** Chapter 2-3 quest stubs, seeded locked; unlocked by later waves' progression rules. */
    fun chapterTwoQuests(): List<StoryProgressEntity> = listOf(
        StoryProgressEntity(
            questId = "foundations-focus-blocks",
            chapterId = 2,
            title = "Complete 5 focus blocks",
            status = "locked",
            progress = 0,
            goal = 5,
        ),
        StoryProgressEntity(
            questId = "foundations-raise-tower",
            chapterId = 2,
            title = "Upgrade a building to level 4",
            status = "locked",
            progress = 0,
            goal = 1,
        ),
        StoryProgressEntity(
            questId = "foundations-unlock-district",
            chapterId = 2,
            title = "Unlock a second district",
            status = "locked",
            progress = 0,
            goal = 1,
        ),
    )

    fun chapterThreeQuests(): List<StoryProgressEntity> = listOf(
        StoryProgressEntity(
            questId = "specialize-choose-path",
            chapterId = 3,
            title = "Choose a specialization path",
            status = "locked",
            progress = 0,
            goal = 1,
        ),
        StoryProgressEntity(
            questId = "specialize-vault-empire",
            chapterId = 3,
            title = "Vault route: bank 300 Gold Dust",
            status = "locked",
            progress = 0,
            goal = 300,
        ),
        StoryProgressEntity(
            questId = "specialize-tech-spire",
            chapterId = 3,
            title = "Spire route: bank 300 Focus",
            status = "locked",
            progress = 0,
            goal = 300,
        ),
    )

    /** Full quest set seeded on first run (idempotent inserts). */
    fun seededQuests(): List<StoryProgressEntity> =
        starterQuests() + chapterTwoQuests() + chapterThreeQuests()
}
