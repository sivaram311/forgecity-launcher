package buzz.delena.forgecity.house

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FilamentDayCycleTest {
    @Test
    fun tDayWrapsOverPeriod() {
        assertEquals(0f, FilamentDayCycle.tDay(0f), 1e-4f)
        assertEquals(0.5f, FilamentDayCycle.tDay(60f), 1e-3f)
        assertEquals(0f, FilamentDayCycle.tDay(120f), 1e-3f)
    }

    @Test
    fun luxStaysAdrenoSafe() {
        listOf(0f, 0.25f, 0.5f, 0.75f, 1f).forEach { t ->
            val s = FilamentDayCycle.sample(t)
            assertTrue("sun $t", s.sunIntensity in 500f..6_200f)
            assertTrue("fill $t", s.fillIntensity in 500f..3_000f)
            assertTrue("dirY $t", s.dirY < 0f)
        }
    }
}

class HouseFacadeFinishingTest {
    @Test
    fun pulseInRange() {
        val p = HouseFacadeFinishing.emissivePulse(2.5f)
        assertTrue(p in 0.55f..1.0f)
    }

    @Test
    fun hasWindowsAndRims() {
        assertTrue(HouseFacadeFinishing.windowPanes.size >= 4)
        assertTrue(HouseFacadeFinishing.cornerRims.size >= 4)
    }
}
