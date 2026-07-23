package buzz.delena.forgecity.power

import android.content.Context
import android.os.Build
import android.os.PowerManager

/**
 * 3D house / ambient quality ladder for Realme mid-range battery + thermal budget.
 *
 * Existing callers keep reading [allowsAmbient] (unchanged semantics).
 * House surfaces should prefer [qualityTier] and the helper getters below.
 */
enum class QualityTier {
    /** Soft shadows, up to 3 active characters, 60 fps hint. */
    HIGH,

    /** No soft shadows, 1 active character, 30 fps hint. */
    MEDIUM,

    /** Minimal / frozen character work, 30 fps hint. */
    LOW,
}

/**
 * Pure policy for [AnimationBudget] so unit tests need no Robolectric.
 *
 * Thermal uses [PowerManager] status codes when available (API 29+); otherwise
 * approximates MEDIUM via power-save while the screen is still interactive.
 */
internal object AnimationBudgetPolicy {
    fun allowsAmbient(
        interactive: Boolean,
        powerSave: Boolean,
        idle: Boolean,
    ): Boolean = interactive && !powerSave && !idle

    /**
     * @param thermalStatus `null` when API < 29 or status unavailable.
     *   Otherwise a [PowerManager] `THERMAL_STATUS_*` int.
     */
    fun qualityTier(
        interactive: Boolean,
        powerSave: Boolean,
        idle: Boolean,
        thermalStatus: Int? = null,
    ): QualityTier {
        if (!interactive || idle) return QualityTier.LOW

        if (thermalStatus != null) {
            if (thermalStatus >= PowerManager.THERMAL_STATUS_SEVERE) {
                return QualityTier.LOW
            }
            if (thermalStatus >= PowerManager.THERMAL_STATUS_MODERATE || powerSave) {
                return QualityTier.MEDIUM
            }
            return QualityTier.HIGH
        }

        // No thermal API: power-save ≈ throttled house; idle / screen-off already LOW.
        return if (powerSave) QualityTier.MEDIUM else QualityTier.HIGH
    }

    fun allowsSoftShadows(tier: QualityTier): Boolean = tier == QualityTier.HIGH

    fun maxActiveCharacters(tier: QualityTier): Int = when (tier) {
        QualityTier.HIGH -> 3
        QualityTier.MEDIUM -> 1
        QualityTier.LOW -> 0
    }

    fun targetFpsHint(tier: QualityTier): Int = when (tier) {
        QualityTier.HIGH -> 60
        QualityTier.MEDIUM, QualityTier.LOW -> 30
    }
}

/**
 * Gates heavy ambient animation and 3D house quality for Realme mid-range budget.
 * Stream B (Android Systems) owns this API; UI only reads the properties below.
 */
class AnimationBudget(context: Context) {
    private val powerManager =
        context.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager

    /** Unchanged: ambient particles / city video gate. */
    val allowsAmbient: Boolean
        get() = AnimationBudgetPolicy.allowsAmbient(
            interactive = powerManager.isInteractive,
            powerSave = powerManager.isPowerSaveMode,
            idle = powerManager.isDeviceIdleMode,
        )

    val qualityTier: QualityTier
        get() = AnimationBudgetPolicy.qualityTier(
            interactive = powerManager.isInteractive,
            powerSave = powerManager.isPowerSaveMode,
            idle = powerManager.isDeviceIdleMode,
            thermalStatus = currentThermalStatusOrNull(),
        )

    val allowsSoftShadows: Boolean
        get() = AnimationBudgetPolicy.allowsSoftShadows(qualityTier)

    val maxActiveCharacters: Int
        get() = AnimationBudgetPolicy.maxActiveCharacters(qualityTier)

    /** Suggested render cadence for Filament / SceneView; not a hard OS cap. */
    val targetFpsHint: Int
        get() = AnimationBudgetPolicy.targetFpsHint(qualityTier)

    private fun currentThermalStatusOrNull(): Int? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
        return try {
            powerManager.currentThermalStatus
        } catch (_: Throwable) {
            null
        }
    }
}
