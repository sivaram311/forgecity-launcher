package buzz.delena.forgecity.house

import kotlin.math.sin
import kotlin.random.Random

/**
 * Production-House-style dust motes for Filament (Grok 0.10.3).
 * Tiny cubes floated in [onFrame]; count capped for Adreno 710.
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
    /** HIGH budget: 64 · MEDIUM/LOW: 32 · disabled: 0 */
    fun countFor(allowsSoftShadows: Boolean, ambientEnabled: Boolean): Int {
        if (!ambientEnabled) return 0
        return if (allowsSoftShadows) 64 else 32
    }

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
        val x = mote.baseX + sin(timeSec * 0.15f + mote.phase) * mote.drift
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
