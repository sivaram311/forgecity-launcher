package buzz.delena.forgecity.house

import kotlin.math.sin
import kotlin.random.Random

/**
 * Soft sphere dust motes (0.11.1 — replaces chunky cubes).
 * Count capped for Adreno 710; radius tiny so they read as air, not snow.
 */
data class DustMote(
    val baseX: Float,
    val baseY: Float,
    val baseZ: Float,
    val phase: Float,
    val speed: Float,
    val drift: Float,
)

object DustMoteCloud {
    /** HIGH budget: 72 · MEDIUM/LOW: 40 · disabled: 0 */
    fun countFor(allowsSoftShadows: Boolean, ambientEnabled: Boolean): Int {
        if (!ambientEnabled) return 0
        return if (allowsSoftShadows) 72 else 40
    }

    /** Sphere radius in meters — soft motes, not bouncing cubes. */
    fun radiusFor(allowsSoftShadows: Boolean): Float =
        if (allowsSoftShadows) 0.014f else 0.011f

    fun seeds(count: Int, seed: Long = 42L): List<DustMote> {
        if (count <= 0) return emptyList()
        val rng = Random(seed)
        return List(count) {
            DustMote(
                baseX = 0.8f + rng.nextFloat() * 7.5f,
                baseY = 0.4f + rng.nextFloat() * 1.8f,
                baseZ = 0.8f + rng.nextFloat() * 7.5f,
                phase = rng.nextFloat() * 6.28f,
                speed = 0.08f + rng.nextFloat() * 0.12f,
                drift = 0.04f + rng.nextFloat() * 0.06f,
            )
        }
    }

    fun positionAt(mote: DustMote, timeSec: Float): Triple<Float, Float, Float> {
        val rise = ((timeSec * mote.speed) + mote.phase) % 2.4f
        val wind = sin(timeSec * 0.15f) * 0.02f
        val x = mote.baseX + sin(timeSec * 0.15f + mote.phase) * mote.drift + wind
        val y = mote.baseY + rise
        val z = mote.baseZ + sin(timeSec * 0.11f + mote.phase * 0.7f) * mote.drift * 0.8f
        return Triple(x, y, z)
    }

    /** Window fill pulse 0.6..1.0 at ~0.8 Hz (Grok atmosphere trick). */
    fun windowPulse(timeSec: Float): Float {
        val s = sin(timeSec * 0.8f * Math.PI.toFloat() * 2f)
        return 0.6f + 0.4f * (0.5f + 0.5f * s)
    }
}
