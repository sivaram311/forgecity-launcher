package buzz.delena.forgecity.city

import buzz.delena.forgecity.usage.UsageXpCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildingHitGeometryTest {
    private data class Fake(val col: Int, val row: Int, val level: Int = 1)

    @Test
    fun depthKeySumsAxes() {
        assertEquals(5, BuildingHitGeometry.depthKey(2, 3))
    }

    @Test
    fun aabbContainsRoofArea() {
        val point = IsoMath.gridToScreen(0f, 0f, IsoLayout.TILE_WIDTH, IsoLayout.TILE_HEIGHT)
        val bounds = BuildingHitGeometry.prismBounds(point, level = 2)
        assertTrue(bounds.contains(point.x, point.y - 20f))
        assertFalse(bounds.contains(point.x + 200f, point.y))
    }

    @Test
    fun frontBuildingWinsOverlap() {
        val front = Fake(col = 2, row = 2, level = 3)
        val back = Fake(col = 0, row = 0, level = 3)
        val point = IsoMath.gridToScreen(2f, 2f, IsoLayout.TILE_WIDTH, IsoLayout.TILE_HEIGHT)
        val hit = BuildingHitGeometry.pickBuilding(
            cityX = point.x,
            cityY = point.y - 10f,
            buildings = listOf(back, front),
            colOf = { it.col },
            rowOf = { it.row },
            levelOf = { it.level },
        )
        assertEquals(front, hit)
    }

    @Test
    fun missReturnsNullFarAway() {
        val only = Fake(0, 0)
        val hit = BuildingHitGeometry.pickBuilding(
            cityX = 900f,
            cityY = 900f,
            buildings = listOf(only),
            colOf = { it.col },
            rowOf = { it.row },
            levelOf = { it.level },
        )
        assertNull(hit)
    }

    @Test
    fun favoritePolicyCapsAtSix() {
        assertTrue(FavoritePolicy.canPin(5, currentlyFavorite = false))
        assertFalse(FavoritePolicy.canPin(6, currentlyFavorite = false))
        assertTrue(FavoritePolicy.canPin(6, currentlyFavorite = true))
    }
}

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

    @Test
    fun duskUsesPurpleOrangeBand() {
        val (_, mid, bottom) = DayNightCycle.skyColors(18)
        assertNotNull(mid)
        assertTrue(bottom > 0xFFE00000)
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
