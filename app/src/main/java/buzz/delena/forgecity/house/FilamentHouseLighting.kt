package buzz.delena.forgecity.house

/**
 * Blue-hour / day-night Filament tuning (Grok realism consult 2026-07-23).
 *
 * See `docs/design/GROK-FILAMENT-REALISM.md`. Intensities kept Adreno-710-safe
 * relative to SceneView defaults (~10k sun); Grok photographic lux scaled down
 * so vertex-colored interiors are not blown out.
 */
object FilamentHouseLighting {
    data class Mode(
        val sunIntensity: Float,
        val fillIntensity: Float,
        val iblIntensity: Float,
        val skyTopR: Float,
        val skyTopG: Float,
        val skyTopB: Float,
        val skyBotR: Float,
        val skyBotG: Float,
        val skyBotB: Float,
        val dirX: Float,
        val dirY: Float,
        val dirZ: Float,
        val fillDirX: Float,
        val fillDirY: Float,
        val fillDirZ: Float,
        /** Camera setExposure() EV-style brightness (Filament direct exposure). */
        val exposure: Float,
        val fogDensity: Float,
        val fogColorR: Float,
        val fogColorG: Float,
        val fogColorB: Float,
        val speechPulseScale: Float,
        val bloomStrength: Float,
    )

    /** Warm daylight interior with cool sky bounce (Production House-adjacent). */
    val day = Mode(
        sunIntensity = 14_000f,
        fillIntensity = 4_500f,
        iblIntensity = 12_000f,
        skyTopR = 0.29f,
        skyTopG = 0.44f,
        skyTopB = 0.65f,
        skyBotR = 0.12f,
        skyBotG = 0.16f,
        skyBotB = 0.27f,
        dirX = 0.55f,
        dirY = -0.72f,
        dirZ = 0.40f,
        fillDirX = -0.35f,
        fillDirY = -0.25f,
        fillDirZ = -0.55f,
        exposure = 1.15f,
        fogDensity = 0.012f,
        fogColorR = 0.55f,
        fogColorG = 0.62f,
        fogColorB = 0.72f,
        speechPulseScale = 1.18f,
        bloomStrength = 0.12f,
    )

    /** Blue-hour / night lamps — cooler sky, warmer window fill. */
    val night = Mode(
        sunIntensity = 1_200f,
        fillIntensity = 6_500f,
        iblIntensity = 3_500f,
        skyTopR = 0.06f,
        skyTopG = 0.10f,
        skyTopB = 0.18f,
        skyBotR = 0.02f,
        skyBotG = 0.03f,
        skyBotB = 0.06f,
        dirX = 0.25f,
        dirY = -0.45f,
        dirZ = 0.55f,
        fillDirX = -0.20f,
        fillDirY = -0.15f,
        fillDirZ = -0.70f,
        exposure = 1.45f,
        fogDensity = 0.008f,
        fogColorR = 0.12f,
        fogColorG = 0.14f,
        fogColorB = 0.22f,
        speechPulseScale = 1.22f,
        bloomStrength = 0.18f,
    )

    fun forNight(isNight: Boolean): Mode = if (isNight) night else day
}
