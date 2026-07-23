package buzz.delena.forgecity.house

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HouseCableRunsTest {
    @Test
    fun droopSagsAtMidspan() {
        val pts = HouseCableRuns.droopSpan(0f, 0.1f, 0f, 2f, 0.1f, 0f, sag = 0.2f, samples = 10)
        val mid = pts[pts.size / 2]
        assertTrue(mid.y < 0.1f)
        assertEquals(0f, pts.first().y - 0.1f, 1e-4f)
        assertEquals(0f, pts.last().y - 0.1f, 1e-4f)
    }

    @Test
    fun defaultRunsSamplePositive() {
        val runs = HouseCableRuns.defaultRuns()
        assertEquals(3, runs.size)
        runs.forEach { wp ->
            val sampled = HouseCableRuns.sampleRun(wp)
            assertTrue(sampled.size > wp.size)
        }
    }
}
