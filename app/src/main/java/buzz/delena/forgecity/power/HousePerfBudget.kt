package buzz.delena.forgecity.power

import android.content.Context
import android.os.Build
import android.os.PowerManager
import java.util.ArrayDeque

/**
 * Optional FPS sample for house surfaces. Not required to be accurate in unit tests;
 * house may pass a real Choreographer-based impl later.
 */
fun interface FpsEstimator {
    /**
     * Approximate frames per second, or a negative value when unknown / not sampling.
     */
    fun estimateFps(): Float
}

/** Always-unknown stub so house can depend on [FpsEstimator] without a real sampler. */
class StubFpsEstimator : FpsEstimator {
    override fun estimateFps(): Float = UNKNOWN_FPS

    companion object {
        const val UNKNOWN_FPS: Float = -1f
    }
}

/**
 * One recorded quality-tier choice for diagnostics / soak notes.
 */
data class QualityTierDecision(
    val tier: QualityTier,
    /** Short machine-readable cause (e.g. `power_save`, `thermal_severe`, `idle`). */
    val reason: String,
    val estimatedFps: Float = StubFpsEstimator.UNKNOWN_FPS,
    val maxActiveCharacters: Int,
    val allowsSoftShadows: Boolean,
    val targetFpsHint: Int,
    val timestampMs: Long,
)

/**
 * Pure policy for [HousePerfBudget]: derive tier (via [AnimationBudgetPolicy]),
 * reason string, and optional FPS-based character clamp. No Android framework needed
 * beyond [PowerManager] thermal constants (same as [AnimationBudgetPolicy]).
 */
internal object HousePerfBudgetPolicy {
    const val LOW_FPS_THRESHOLD: Float = 25f

    fun reasonFor(
        interactive: Boolean,
        powerSave: Boolean,
        idle: Boolean,
        thermalStatus: Int?,
        tier: QualityTier,
    ): String {
        if (!interactive) return "not_interactive"
        if (idle) return "idle"
        if (thermalStatus != null && thermalStatus >= PowerManager.THERMAL_STATUS_SEVERE) {
            return "thermal_severe"
        }
        if (thermalStatus != null && thermalStatus >= PowerManager.THERMAL_STATUS_MODERATE) {
            return "thermal_moderate"
        }
        if (powerSave) return "power_save"
        return when (tier) {
            QualityTier.HIGH -> "nominal"
            QualityTier.MEDIUM -> "medium"
            QualityTier.LOW -> "low"
        }
    }

    /**
     * When a positive FPS sample is below [LOW_FPS_THRESHOLD], clamp characters to 0
     * (same as LOW) without changing the reported [QualityTier] from power/thermal policy.
     */
    fun effectiveMaxActiveCharacters(
        tier: QualityTier,
        estimatedFps: Float,
    ): Int {
        val base = AnimationBudgetPolicy.maxActiveCharacters(tier)
        if (estimatedFps >= 0f && estimatedFps < LOW_FPS_THRESHOLD) {
            return 0
        }
        return base
    }

    fun decide(
        interactive: Boolean,
        powerSave: Boolean,
        idle: Boolean,
        thermalStatus: Int? = null,
        estimatedFps: Float = StubFpsEstimator.UNKNOWN_FPS,
        timestampMs: Long = 0L,
    ): QualityTierDecision {
        val tier = AnimationBudgetPolicy.qualityTier(
            interactive = interactive,
            powerSave = powerSave,
            idle = idle,
            thermalStatus = thermalStatus,
        )
        return QualityTierDecision(
            tier = tier,
            reason = reasonFor(interactive, powerSave, idle, thermalStatus, tier),
            estimatedFps = estimatedFps,
            maxActiveCharacters = effectiveMaxActiveCharacters(tier, estimatedFps),
            allowsSoftShadows = AnimationBudgetPolicy.allowsSoftShadows(tier),
            targetFpsHint = AnimationBudgetPolicy.targetFpsHint(tier),
            timestampMs = timestampMs,
        )
    }
}

/** Bounded newest-last ring used by [HousePerfBudget]; pure for unit tests. */
internal class DecisionHistory(capacity: Int = HousePerfBudget.DEFAULT_HISTORY) {
    private val capacity = capacity.coerceAtLeast(1)
    private val deque = ArrayDeque<QualityTierDecision>(this.capacity)

    @Volatile
    var last: QualityTierDecision? = null
        private set

    fun record(decision: QualityTierDecision) {
        last = decision
        synchronized(deque) {
            if (deque.size >= capacity) {
                deque.removeFirst()
            }
            deque.addLast(decision)
        }
    }

    /** Newest-first. */
    fun recent(): List<QualityTierDecision> = synchronized(deque) {
        deque.toList().asReversed()
    }
}

/**
 * Records house quality-tier decisions for Wave 3 soak / Realme #16 notes.
 *
 * House surfaces should call [refresh] (or read [lastDecision]) when binding
 * characters / soft shadows; pass a real [FpsEstimator] when available.
 *
 * Convenience getters mirror the latest decision so house can pass
 * `housePerfBudget.maxActiveCharacters` like [AnimationBudget].
 */
class HousePerfBudget(
    private val sample: () -> QualityTierDecision,
    historyCapacity: Int = DEFAULT_HISTORY,
) {
    private val history = DecisionHistory(historyCapacity)

    val lastDecision: QualityTierDecision?
        get() = history.last

    val qualityTier: QualityTier
        get() = lastDecision?.tier ?: QualityTier.HIGH

    val maxActiveCharacters: Int
        get() = lastDecision?.maxActiveCharacters
            ?: AnimationBudgetPolicy.maxActiveCharacters(QualityTier.HIGH)

    val allowsSoftShadows: Boolean
        get() = lastDecision?.allowsSoftShadows ?: true

    val targetFpsHint: Int
        get() = lastDecision?.targetFpsHint
            ?: AnimationBudgetPolicy.targetFpsHint(QualityTier.HIGH)

    /** Newest-first copy of recent decisions (capped). */
    fun recentDecisions(): List<QualityTierDecision> = history.recent()

    /**
     * Re-sample power/thermal (+ optional FPS), record, and return the decision.
     */
    fun refresh(): QualityTierDecision {
        val decision = sample()
        history.record(decision)
        return decision
    }

    companion object {
        const val DEFAULT_HISTORY: Int = 32

        fun create(
            context: Context,
            fpsEstimator: FpsEstimator = StubFpsEstimator(),
            clockMs: () -> Long = { System.currentTimeMillis() },
            historyCapacity: Int = DEFAULT_HISTORY,
        ): HousePerfBudget {
            val app = context.applicationContext
            val powerManager = app.getSystemService(Context.POWER_SERVICE) as PowerManager
            return HousePerfBudget(
                sample = {
                    val fps = fpsEstimator.estimateFps()
                    HousePerfBudgetPolicy.decide(
                        interactive = powerManager.isInteractive,
                        powerSave = powerManager.isPowerSaveMode,
                        idle = powerManager.isDeviceIdleMode,
                        thermalStatus = currentThermalStatusOrNull(powerManager),
                        estimatedFps = fps,
                        timestampMs = clockMs(),
                    )
                },
                historyCapacity = historyCapacity,
            )
        }

        private fun currentThermalStatusOrNull(powerManager: PowerManager): Int? {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
            return try {
                powerManager.currentThermalStatus
            } catch (_: Throwable) {
                null
            }
        }
    }
}
