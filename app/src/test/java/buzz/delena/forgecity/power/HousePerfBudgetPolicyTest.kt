package buzz.delena.forgecity.power

import android.os.PowerManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HousePerfBudgetPolicyTest {

    @Test
    fun decide_nominal_isHigh() {
        val d = HousePerfBudgetPolicy.decide(
            interactive = true,
            powerSave = false,
            idle = false,
            thermalStatus = null,
            estimatedFps = StubFpsEstimator.UNKNOWN_FPS,
            timestampMs = 100L,
        )
        assertEquals(QualityTier.HIGH, d.tier)
        assertEquals("nominal", d.reason)
        assertEquals(3, d.maxActiveCharacters)
        assertTrue(d.allowsSoftShadows)
        assertEquals(60, d.targetFpsHint)
        assertEquals(100L, d.timestampMs)
    }

    @Test
    fun decide_powerSave_isMedium_andDropsCharacters() {
        val d = HousePerfBudgetPolicy.decide(
            interactive = true,
            powerSave = true,
            idle = false,
            thermalStatus = null,
        )
        assertEquals(QualityTier.MEDIUM, d.tier)
        assertEquals("power_save", d.reason)
        assertEquals(1, d.maxActiveCharacters)
        assertFalse(d.allowsSoftShadows)
        assertEquals(30, d.targetFpsHint)
    }

    @Test
    fun decide_thermalSevere_isLow() {
        val d = HousePerfBudgetPolicy.decide(
            interactive = true,
            powerSave = false,
            idle = false,
            thermalStatus = PowerManager.THERMAL_STATUS_SEVERE,
        )
        assertEquals(QualityTier.LOW, d.tier)
        assertEquals("thermal_severe", d.reason)
        assertEquals(0, d.maxActiveCharacters)
    }

    @Test
    fun decide_idle_isLow() {
        val d = HousePerfBudgetPolicy.decide(
            interactive = true,
            powerSave = false,
            idle = true,
        )
        assertEquals(QualityTier.LOW, d.tier)
        assertEquals("idle", d.reason)
    }

    @Test
    fun effectiveMaxActiveCharacters_clampsOnLowFpsSample() {
        assertEquals(
            0,
            HousePerfBudgetPolicy.effectiveMaxActiveCharacters(
                QualityTier.HIGH,
                estimatedFps = 20f,
            ),
        )
        assertEquals(
            3,
            HousePerfBudgetPolicy.effectiveMaxActiveCharacters(
                QualityTier.HIGH,
                estimatedFps = StubFpsEstimator.UNKNOWN_FPS,
            ),
        )
        assertEquals(
            3,
            HousePerfBudgetPolicy.effectiveMaxActiveCharacters(
                QualityTier.HIGH,
                estimatedFps = 30f,
            ),
        )
    }

    @Test
    fun stubFpsEstimator_reportsUnknown() {
        assertEquals(StubFpsEstimator.UNKNOWN_FPS, StubFpsEstimator().estimateFps())
    }

    @Test
    fun decisionHistory_keepsNewestAndCaps() {
        val history = DecisionHistory(capacity = 2)
        history.record(
            HousePerfBudgetPolicy.decide(
                interactive = true,
                powerSave = false,
                idle = false,
                timestampMs = 1L,
            ),
        )
        history.record(
            HousePerfBudgetPolicy.decide(
                interactive = true,
                powerSave = true,
                idle = false,
                timestampMs = 2L,
            ),
        )
        history.record(
            HousePerfBudgetPolicy.decide(
                interactive = true,
                powerSave = false,
                idle = true,
                timestampMs = 3L,
            ),
        )
        val recent = history.recent()
        assertEquals(2, recent.size)
        assertEquals(3L, recent[0].timestampMs)
        assertEquals(2L, recent[1].timestampMs)
        assertEquals(QualityTier.LOW, history.last?.tier)
    }

    @Test
    fun housePerfBudget_refresh_recordsViaInjectedSample() {
        var tick = 0L
        val budget = HousePerfBudget(
            sample = {
                tick += 1L
                HousePerfBudgetPolicy.decide(
                    interactive = true,
                    powerSave = tick > 1L,
                    idle = false,
                    estimatedFps = if (tick > 1L) 18f else StubFpsEstimator.UNKNOWN_FPS,
                    timestampMs = tick,
                )
            },
            historyCapacity = 8,
        )
        val first = budget.refresh()
        assertEquals(QualityTier.HIGH, first.tier)
        assertEquals(3, first.maxActiveCharacters)

        val second = budget.refresh()
        assertEquals(QualityTier.MEDIUM, second.tier)
        // power_save → 1 char, then low FPS clamp → 0
        assertEquals(0, second.maxActiveCharacters)
        assertEquals(second, budget.lastDecision)
        assertEquals(0, budget.maxActiveCharacters)
    }
}
