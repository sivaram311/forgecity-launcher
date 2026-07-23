package buzz.delena.forgecity.house

/**
 * Day/night Filament tuning for Adreno 710 (Grok 4.1 consult 2026-07-23).
 *
 * Intensities are scaled from the consult table into SceneView-friendly lux so
 * diffuse vertex-colored shells are not blown out (SceneView defaults ~10k sun).
 * See `docs/design/GROK-FILAMENT-LIGHTING.md`.
 */
object FilamentHouseLighting {
    data class Mode(
        val sunIntensity: Float,
        val skyR: Float,
        val skyG: Float,
        val skyB: Float,
        /** Directional light direction (Filament light direction vector). */
        val dirX: Float,
        val dirY: Float,
        val dirZ: Float,
        val speechPulseScale: Float,
    )

    val day = Mode(
        sunIntensity = 12_000f,
        skyR = 0.65f,
        skyG = 0.80f,
        skyB = 1.00f,
        dirX = 0.55f,
        dirY = -0.75f,
        dirZ = 0.35f,
        speechPulseScale = 1.2f,
    )

    val night = Mode(
        sunIntensity = 900f,
        skyR = 0.08f,
        skyG = 0.08f,
        skyB = 0.25f,
        dirX = 0.35f,
        dirY = -0.55f,
        dirZ = 0.45f,
        speechPulseScale = 1.15f,
    )

    fun forNight(isNight: Boolean): Mode = if (isNight) night else day
}
