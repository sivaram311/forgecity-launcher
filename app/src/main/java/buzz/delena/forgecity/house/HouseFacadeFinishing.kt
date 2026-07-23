package buzz.delena.forgecity.house

import kotlin.math.sin

/**
 * Window glow quads + exterior corner rim strips (0.11 finishing).
 * Positions match `house_shell.glb` / [HouseRoom] meter layout.
 */
object HouseFacadeFinishing {
    data class Strip(
        val x: Float,
        val y: Float,
        val z: Float,
        val sx: Float,
        val sy: Float,
        val sz: Float,
        /** true = cool rim (#7ec8e3), false = warm (#e8b86d) */
        val cool: Boolean,
    )

    data class WindowPane(
        val x: Float,
        val y: Float,
        val z: Float,
        val sx: Float,
        val sy: Float,
        val sz: Float,
        val warm: Boolean,
    )

    /** Vertical corner rims on the main shell (0..9 × 0..9). */
    val cornerRims: List<Strip> = listOf(
        Strip(0.04f, 1.25f, 0.04f, 0.04f, 2.4f, 0.04f, cool = true),
        Strip(8.96f, 1.25f, 0.04f, 0.04f, 2.4f, 0.04f, cool = false),
        Strip(0.04f, 1.25f, 8.96f, 0.04f, 2.4f, 0.04f, cool = false),
        Strip(8.96f, 1.25f, 8.96f, 0.04f, 2.4f, 0.04f, cool = true),
        Strip(6.98f, 1.25f, 3.02f, 0.035f, 2.2f, 0.035f, cool = true),
        Strip(6.98f, 1.25f, 5.98f, 0.035f, 2.2f, 0.035f, cool = false),
    )

    /** Soft emissive stand-ins over known window openings. */
    val windowPanes: List<WindowPane> = listOf(
        WindowPane(1.5f, 1.35f, 0.02f, 0.9f, 0.85f, 0.03f, warm = true),
        WindowPane(5.0f, 1.35f, 0.02f, 1.1f, 0.85f, 0.03f, warm = true),
        WindowPane(0.02f, 1.35f, 4.5f, 0.03f, 0.85f, 0.9f, warm = false),
        WindowPane(8.98f, 1.35f, 4.5f, 0.03f, 0.85f, 1.0f, warm = true),
        WindowPane(1.5f, 1.35f, 8.98f, 0.9f, 0.85f, 0.03f, warm = false),
        WindowPane(5.0f, 1.35f, 8.98f, 1.0f, 0.85f, 0.03f, warm = true),
    )

    /** PH-style ~4s sine emissive pulse (0.55..1.0). */
    fun emissivePulse(timeSec: Float): Float {
        val s = sin(timeSec * (Math.PI.toFloat() * 2f / 4f))
        return 0.55f + 0.45f * (0.5f + 0.5f * s)
    }
}
