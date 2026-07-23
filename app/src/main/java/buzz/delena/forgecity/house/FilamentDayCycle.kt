package buzz.delena.forgecity.house

import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin

/**
 * Filament-safe day-cycle sampling (~120s loop). Lux capped for Adreno 710;
 * colors lerp like Production House LightsRig but never drop to near-black in day mode.
 *
 * See `docs/design/GROK-0.11-HUMANOID.md`.
 */
object FilamentDayCycle {
    const val PERIOD_SEC = 120f

    data class Sample(
        val sunColor: Color,
        val sunIntensity: Float,
        val fillIntensity: Float,
        val fillColor: Color,
        val skyR: Float,
        val skyG: Float,
        val skyB: Float,
        val dirX: Float,
        val dirY: Float,
        val dirZ: Float,
        val hemiSky: Color,
        val hemiGround: Color,
        val hemiIntensity: Float,
    )

    private data class Stop(
        val t: Float,
        val sunArgb: Long,
        val sunLux: Float,
        val fillLux: Float,
        val skyR: Float,
        val skyG: Float,
        val skyB: Float,
        val hemiArgb: Long,
    )

    private val stops = listOf(
        Stop(0.00f, 0xFFFFB366, 5_200f, 1_800f, 0.55f, 0.72f, 0.95f, 0xFF8AA4C8),
        Stop(0.25f, 0xFFFFE4B5, 5_800f, 1_200f, 0.48f, 0.65f, 0.92f, 0xFF7A96B8),
        Stop(0.50f, 0xFFFFF8E7, 6_100f, 900f, 0.42f, 0.58f, 0.88f, 0xFF6A86A8),
        Stop(0.75f, 0xFFFFCC99, 5_400f, 1_500f, 0.50f, 0.68f, 0.93f, 0xFF809AB8),
        Stop(1.00f, 0xFFFF9966, 4_800f, 2_100f, 0.58f, 0.75f, 0.96f, 0xFF94AEC8),
    )

    fun tDay(timeSec: Float): Float {
        val p = ((timeSec % PERIOD_SEC) + PERIOD_SEC) % PERIOD_SEC
        return p / PERIOD_SEC
    }

    fun sample(tDay: Float): Sample {
        val t = tDay.coerceIn(0f, 1f)
        var i = 0
        while (i < stops.lastIndex && stops[i + 1].t < t) i++
        val a = stops[i]
        val b = stops[minOf(i + 1, stops.lastIndex)]
        val span = (b.t - a.t).takeIf { it > 1e-5f } ?: 1f
        val u = ((t - a.t) / span).coerceIn(0f, 1f)

        val sun = lerpColor(a.sunArgb, b.sunArgb, u)
        val hemi = lerpColor(a.hemiArgb, b.hemiArgb, u)
        val sunLux = a.sunLux + (b.sunLux - a.sunLux) * u
        val fillLux = a.fillLux + (b.fillLux - a.fillLux) * u
        val skyR = a.skyR + (b.skyR - a.skyR) * u
        val skyG = a.skyG + (b.skyG - a.skyG) * u
        val skyB = a.skyB + (b.skyB - a.skyB) * u

        // Sun orbit (PH LightsRig) → Filament lightDirection (toward scene).
        val az = t * Math.PI.toFloat() * 2f
        val el = sin(t * Math.PI.toFloat()) * 0.9f + 0.15f
        val cosEl = cos(el)
        val px = cos(az) * cosEl
        val py = sin(el).coerceAtLeast(0.12f)
        val pz = sin(az) * cosEl
        val len = kotlin.math.sqrt(px * px + py * py + pz * pz).coerceAtLeast(1e-4f)

        val intNorm = ((sunLux - 4_800f) / 1_300f).coerceIn(0f, 1f)
        return Sample(
            sunColor = sun,
            sunIntensity = sunLux.coerceIn(500f, 6_200f),
            fillIntensity = fillLux.coerceIn(500f, 3_000f),
            fillColor = Color(0xFFFFE0B0),
            skyR = skyR,
            skyG = skyG,
            skyB = skyB,
            dirX = -px / len,
            dirY = -py / len,
            dirZ = -pz / len,
            hemiSky = hemi,
            hemiGround = Color(0xFF3A3530),
            hemiIntensity = (0.25f + intNorm * 0.35f) * 1_200f,
        )
    }

    private fun lerpColor(a: Long, b: Long, u: Float): Color {
        val ca = Color(a)
        val cb = Color(b)
        return Color(
            red = ca.red + (cb.red - ca.red) * u,
            green = ca.green + (cb.green - ca.green) * u,
            blue = ca.blue + (cb.blue - ca.blue) * u,
            alpha = 1f,
        )
    }
}
