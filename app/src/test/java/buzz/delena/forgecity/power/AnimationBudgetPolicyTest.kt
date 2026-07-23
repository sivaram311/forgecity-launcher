package buzz.delena.forgecity.power

import android.os.PowerManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnimationBudgetPolicyTest {

    @Test
    fun allowsAmbient_unchangedSemantics() {
        assertTrue(
            AnimationBudgetPolicy.allowsAmbient(
                interactive = true,
                powerSave = false,
                idle = false,
            ),
        )
        assertFalse(
            AnimationBudgetPolicy.allowsAmbient(
                interactive = true,
                powerSave = true,
                idle = false,
            ),
        )
        assertFalse(
            AnimationBudgetPolicy.allowsAmbient(
                interactive = false,
                powerSave = false,
                idle = false,
            ),
        )
        assertFalse(
            AnimationBudgetPolicy.allowsAmbient(
                interactive = true,
                powerSave = false,
                idle = true,
            ),
        )
    }

    @Test
    fun qualityTier_withoutThermal_approximatesViaPowerSaveAndIdle() {
        assertEquals(
            QualityTier.HIGH,
            AnimationBudgetPolicy.qualityTier(
                interactive = true,
                powerSave = false,
                idle = false,
                thermalStatus = null,
            ),
        )
        assertEquals(
            QualityTier.MEDIUM,
            AnimationBudgetPolicy.qualityTier(
                interactive = true,
                powerSave = true,
                idle = false,
                thermalStatus = null,
            ),
        )
        assertEquals(
            QualityTier.LOW,
            AnimationBudgetPolicy.qualityTier(
                interactive = true,
                powerSave = false,
                idle = true,
                thermalStatus = null,
            ),
        )
        assertEquals(
            QualityTier.LOW,
            AnimationBudgetPolicy.qualityTier(
                interactive = false,
                powerSave = false,
                idle = false,
                thermalStatus = null,
            ),
        )
    }

    @Test
    fun qualityTier_withThermal_dropsOnModerateAndSevere() {
        assertEquals(
            QualityTier.HIGH,
            AnimationBudgetPolicy.qualityTier(
                interactive = true,
                powerSave = false,
                idle = false,
                thermalStatus = PowerManager.THERMAL_STATUS_NONE,
            ),
        )
        assertEquals(
            QualityTier.HIGH,
            AnimationBudgetPolicy.qualityTier(
                interactive = true,
                powerSave = false,
                idle = false,
                thermalStatus = PowerManager.THERMAL_STATUS_LIGHT,
            ),
        )
        assertEquals(
            QualityTier.MEDIUM,
            AnimationBudgetPolicy.qualityTier(
                interactive = true,
                powerSave = false,
                idle = false,
                thermalStatus = PowerManager.THERMAL_STATUS_MODERATE,
            ),
        )
        assertEquals(
            QualityTier.LOW,
            AnimationBudgetPolicy.qualityTier(
                interactive = true,
                powerSave = false,
                idle = false,
                thermalStatus = PowerManager.THERMAL_STATUS_SEVERE,
            ),
        )
        assertEquals(
            QualityTier.MEDIUM,
            AnimationBudgetPolicy.qualityTier(
                interactive = true,
                powerSave = true,
                idle = false,
                thermalStatus = PowerManager.THERMAL_STATUS_NONE,
            ),
        )
    }

    @Test
    fun helpers_matchTier() {
        assertTrue(AnimationBudgetPolicy.allowsSoftShadows(QualityTier.HIGH))
        assertFalse(AnimationBudgetPolicy.allowsSoftShadows(QualityTier.MEDIUM))
        assertFalse(AnimationBudgetPolicy.allowsSoftShadows(QualityTier.LOW))

        assertEquals(3, AnimationBudgetPolicy.maxActiveCharacters(QualityTier.HIGH))
        assertEquals(1, AnimationBudgetPolicy.maxActiveCharacters(QualityTier.MEDIUM))
        assertEquals(0, AnimationBudgetPolicy.maxActiveCharacters(QualityTier.LOW))

        assertEquals(60, AnimationBudgetPolicy.targetFpsHint(QualityTier.HIGH))
        assertEquals(30, AnimationBudgetPolicy.targetFpsHint(QualityTier.MEDIUM))
        assertEquals(30, AnimationBudgetPolicy.targetFpsHint(QualityTier.LOW))
    }
}
