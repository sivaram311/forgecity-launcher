package buzz.delena.forgecity.city

/**
 * Pure mapping from [District] to a roof/top silhouette style.
 *
 * Silhouettes are decorative rooftop treatments drawn above the shared prism.
 * They do NOT change the base footprint or [BuildingHitGeometry.prismHeight],
 * so hit geometry and its unit tests stay valid.
 */
enum class RoofStyle {
    /** Forge — tall central spire. */
    SPIRE,

    /** Vault — flatter gold cap / crown. */
    GOLD_CAP,

    /** Nexus — thin teal comms antenna with a beacon. */
    ANTENNA,

    /** Arena — angular twin peaks. */
    ANGULAR,

    /** Garden — soft rounded dome. */
    SOFT,

    /** Archive — stepped ziggurat tiers. */
    STEPPED,

    /** Custom / fallback — simple low marker. */
    DEFAULT,
}

object DistrictSilhouette {
    fun of(district: District): RoofStyle = when (district) {
        District.FORGE -> RoofStyle.SPIRE
        District.VAULT -> RoofStyle.GOLD_CAP
        District.NEXUS -> RoofStyle.ANTENNA
        District.ARENA -> RoofStyle.ANGULAR
        District.GARDEN -> RoofStyle.SOFT
        District.ARCHIVE -> RoofStyle.STEPPED
        District.CUSTOM -> RoofStyle.DEFAULT
    }
}
