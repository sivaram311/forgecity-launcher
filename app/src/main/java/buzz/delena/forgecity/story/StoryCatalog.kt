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
}
