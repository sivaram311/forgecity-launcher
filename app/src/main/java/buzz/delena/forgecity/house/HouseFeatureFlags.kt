package buzz.delena.forgecity.house

/**
 * Compile-time gates for the 3D house scene and city video backdrop.
 *
 * When [use3dHouse] is false, [buzz.delena.forgecity.ui.CityCanvas] remains the home
 * surface fallback. Wave 1 sets [use3dHouse] true for the procedural HouseHomeSurface.
 */
object HouseFeatureFlags {
    /** 3D house scene — Wave 1 procedural HouseHomeSurface. */
    const val use3dHouse: Boolean = true

    /** City looping video backdrop (independent of assistant background-video prefs). */
    const val useCityVideo: Boolean = false
}
