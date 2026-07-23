package buzz.delena.forgecity.house

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DustMoteCloudTest {
    @Test
    fun countGatesOnBudget() {
        assertEquals(0, DustMoteCloud.countFor(allowsSoftShadows = true, ambientEnabled = false))
        assertEquals(64, DustMoteCloud.countFor(allowsSoftShadows = true, ambientEnabled = true))
        assertEquals(32, DustMoteCloud.countFor(allowsSoftShadows = false, ambientEnabled = true))
    }

    @Test
    fun seedsStableAndBounded() {
        val a = DustMoteCloud.seeds(16)
        val b = DustMoteCloud.seeds(16)
        assertEquals(a, b)
        assertEquals(16, a.size)
        a.forEach { m ->
            assertTrue(m.baseX in 0.5f..9f)
            assertTrue(m.baseZ in 0.5f..9f)
        }
    }

    @Test
    fun windowPulseInRange() {
        val p = DustMoteCloud.windowPulse(1.25f)
        assertTrue(p in 0.6f..1.0f)
    }
}
