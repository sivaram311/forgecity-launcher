package buzz.delena.forgecity.house

/**
 * Adreno-safe HDR IBL + fresnel stand-ins for gap #8 (0.14).
 *
 * Caps from Grok: no 4K HDR / custom filamat / bare EV; day IBL ~1800;
 * skin/cloth reflectance instead of custom fresnel shaders; rim DIRECTIONAL ~1200 lux.
 */
object FilamentHouseIbl {
    /** Soft 256×128 Radiance HDR under `assets/` (~128 KB). */
    const val HDR_ASSET = "filament/house_ibl_256.hdr"

    const val DAY_IBL = 1_800f
    const val NIGHT_IBL = 900f
    const val RIM_LUX = 1_200f

    /** Dielectric reflectance (Filament) — fresnel stand-in without custom shaders. */
    const val SKIN_REFLECTANCE = 0.04f
    const val CLOTH_REFLECTANCE = 0.035f
    const val HAIR_REFLECTANCE = 0.02f
    const val RIM_STRIP_REFLECTANCE = 0.48f
    const val GLASS_REFLECTANCE = 0.55f

    fun iblIntensity(night: Boolean, dayHemi: Float?): Float {
        if (night) return NIGHT_IBL
        val hemi = dayHemi ?: DAY_IBL
        // Map day-cycle hemi (~300–720) up into Adreno-safe HDR band.
        return (hemi * 2.2f).coerceIn(1_200f, 2_000f)
    }

    fun rimIntensity(ambientEnabled: Boolean, night: Boolean, pulse: Float): Float {
        if (!ambientEnabled) return RIM_LUX * 0.25f
        val base = if (night) RIM_LUX * 1.1f else RIM_LUX
        return base * (0.85f + 0.25f * pulse)
    }
}
