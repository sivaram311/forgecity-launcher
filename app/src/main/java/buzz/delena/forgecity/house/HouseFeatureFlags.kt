package buzz.delena.forgecity.house

/**
 * Compile-time gates for the 3D house scene and city video backdrop.
 *
 * When [use3dHouse] is false, [buzz.delena.forgecity.ui.CityCanvas] remains the home
 * surface fallback. When [useFilamentHouse] is true, SceneView/Filament is preferred
 * over the procedural Compose [HouseHomeSurface].
 */
object HouseFeatureFlags {
    /** 3D house scene (Filament or procedural Compose). */
    const val use3dHouse: Boolean = true

    /** Filament / SceneView house (0.10+). Falls back to procedural Compose if false. */
    const val useFilamentHouse: Boolean = true

    /** City looping video backdrop (independent of assistant background-video prefs). */
    const val useCityVideo: Boolean = false
}
