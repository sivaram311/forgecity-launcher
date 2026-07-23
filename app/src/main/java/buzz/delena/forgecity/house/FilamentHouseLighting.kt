package buzz.delena.forgecity.house

/**
 * Filament lighting tuned after white-screen failure (Grok 2026-07-23).
 *
 * SceneView defaults use [setExposure](aperture, shutter, iso) ≈ f/12, 1/200, ISO 200.
 * Passing a bare float like `1.15` is treated as EV100 and over-exposes to solid white
 * while the camera manipulator still works.
 *
 * See `docs/design/GROK-WHITE-SCREEN.md`.
 */
object FilamentHouseLighting {
    data class Mode(
        val sunIntensity: Float,
        val fillIntensity: Float,
        val iblIntensity: Float,
        val skyTopR: Float,
        val skyTopG: Float,
        val skyTopB: Float,
        val dirX: Float,
        val dirY: Float,
        val dirZ: Float,
        val fillDirX: Float,
        val fillDirY: Float,
        val fillDirZ: Float,
        /** Photographic aperture (f-stops). Lower → brighter. */
        val aperture: Float,
        val shutterSpeed: Float,
        val iso: Float,
        val fogDensity: Float,
        val fogColorR: Float,
        val fogColorG: Float,
        val fogColorB: Float,
        val speechPulseScale: Float,
        /** Bloom off by default until mood is stable; keep strength tiny if enabled. */
        val bloomEnabled: Boolean,
        val bloomStrength: Float,
        val bloomThreshold: Float,
    )

    val day = Mode(
        sunIntensity = 5_000f,
        fillIntensity = 2_000f,
        iblIntensity = 1_800f,
        skyTopR = 0.35f,
        skyTopG = 0.48f,
        skyTopB = 0.62f,
        dirX = 0.55f,
        dirY = -0.72f,
        dirZ = 0.40f,
        fillDirX = -0.35f,
        fillDirY = -0.25f,
        fillDirZ = -0.55f,
        aperture = 12.0f,
        shutterSpeed = 1.0f / 200.0f,
        iso = 200.0f,
        fogDensity = 0.006f,
        fogColorR = 0.45f,
        fogColorG = 0.52f,
        fogColorB = 0.60f,
        speechPulseScale = 1.12f,
        bloomEnabled = false,
        bloomStrength = 0.08f,
        bloomThreshold = 2.0f, // retained for docs; Filament binding may ignore
    )

    val night = Mode(
        sunIntensity = 600f,
        fillIntensity = 2_800f,
        iblIntensity = 900f,
        skyTopR = 0.05f,
        skyTopG = 0.07f,
        skyTopB = 0.12f,
        dirX = 0.25f,
        dirY = -0.45f,
        dirZ = 0.55f,
        fillDirX = -0.20f,
        fillDirY = -0.15f,
        fillDirZ = -0.70f,
        aperture = 8.0f,
        shutterSpeed = 1.0f / 60.0f,
        iso = 400.0f,
        fogDensity = 0.004f,
        fogColorR = 0.08f,
        fogColorG = 0.09f,
        fogColorB = 0.14f,
        speechPulseScale = 1.15f,
        bloomEnabled = true,
        bloomStrength = 0.10f,
        bloomThreshold = 1.5f,
    )

    fun forNight(isNight: Boolean): Mode = if (isNight) night else day
}
