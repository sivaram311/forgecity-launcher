package buzz.delena.forgecity.usage

/**
 * Pure XP conversion from foreground usage minutes.
 * Stream B owns formula; Stream C persists results.
 */
object UsageXpCalculator {
    data class Gains(
        val power: Int,
        val focus: Int,
        val goldDust: Int,
        val scrap: Int,
    )

    fun fromForegroundMinutes(
        totalMinutes: Long,
        productiveMinutes: Long,
        financeMinutes: Long,
        organizedAppCount: Int,
    ): Gains {
        val safeTotal = totalMinutes.coerceAtLeast(0)
        val safeProd = productiveMinutes.coerceIn(0, safeTotal)
        val safeFin = financeMinutes.coerceIn(0, safeTotal)
        return Gains(
            power = (safeTotal / 5).toInt().coerceAtMost(40),
            focus = (safeProd / 3).toInt().coerceAtMost(50),
            goldDust = (safeFin / 4).toInt().coerceAtMost(30),
            scrap = (organizedAppCount / 5).coerceAtMost(20),
        )
    }

    fun levelForLaunchCount(launches: Int): Int = when {
        launches >= 40 -> 5
        launches >= 20 -> 4
        launches >= 10 -> 3
        launches >= 4 -> 2
        else -> 1
    }
}
