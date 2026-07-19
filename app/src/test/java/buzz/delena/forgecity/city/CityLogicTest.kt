package buzz.delena.forgecity.city

import buzz.delena.forgecity.usage.UsageXpCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IsoMathTest {
    @Test
    fun projectsOriginToZero() {
        val point = IsoMath.gridToScreen(0f, 0f, 88f, 44f)
        assertEquals(0f, point.x, 0.001f)
        assertEquals(0f, point.y, 0.001f)
    }

    @Test
    fun projectsNeighborCellDiagonally() {
        val point = IsoMath.gridToScreen(1f, 0f, 88f, 44f)
        assertEquals(44f, point.x, 0.001f)
        assertEquals(22f, point.y, 0.001f)
    }
}

class DistrictClassifierTest {
    @Test
    fun classifiesTradingAppsIntoVault() {
        assertEquals(
            District.VAULT,
            DistrictClassifier.classify("com.metaquotes.metatrader5", "MetaTrader 5"),
        )
    }

    @Test
    fun classifiesCodingToolsIntoForge() {
        assertEquals(
            District.FORGE,
            DistrictClassifier.classify("com.termux", "Termux"),
        )
    }

    @Test
    fun defaultsUnknownAppsToForge() {
        assertTrue(DistrictClassifier.classify("com.example.obscure", "Obscure") == District.FORGE)
    }
}

class DayNightCycleTest {
    @Test
    fun nightWindowIncludesEveningAndDawn() {
        assertTrue(DayNightCycle.isNight(21))
        assertTrue(DayNightCycle.isNight(3))
        assertFalse(DayNightCycle.isNight(12))
    }

    @Test
    fun starsVisibleAtNightOnly() {
        assertTrue(DayNightCycle.starAlpha(22) > 0.5f)
        assertEquals(0f, DayNightCycle.starAlpha(14), 0.001f)
    }
}

class UsageXpCalculatorTest {
    @Test
    fun convertsMinutesIntoCappedGains() {
        val gains = UsageXpCalculator.fromForegroundMinutes(
            totalMinutes = 120,
            productiveMinutes = 60,
            financeMinutes = 40,
            organizedAppCount = 25,
        )
        assertEquals(24, gains.power)
        assertEquals(20, gains.focus)
        assertEquals(10, gains.goldDust)
        assertEquals(5, gains.scrap)
    }

    @Test
    fun launchCountsMapToBuildingLevels() {
        assertEquals(1, UsageXpCalculator.levelForLaunchCount(0))
        assertEquals(2, UsageXpCalculator.levelForLaunchCount(4))
        assertEquals(5, UsageXpCalculator.levelForLaunchCount(99))
    }
}
