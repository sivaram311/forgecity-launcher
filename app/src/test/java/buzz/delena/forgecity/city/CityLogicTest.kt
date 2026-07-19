package buzz.delena.forgecity.city

import org.junit.Assert.assertEquals
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
