1) Gradle
```kotlin
// app/build.gradle.kts
implementation("io.github.sceneview:sceneview:2.3.0")          // Filament 1.52, minSdk 26
implementation("androidx.compose.ui:ui:1.7.0")
implementation("androidx.compose.ui:ui-viewbinding:1.7.0")
```

2) Compose integration
```kotlin
AndroidView(
    factory = { ctx -> SceneView(ctx).apply { 
        scene = loadHouseScene() 
        onTouch = { pickAppHotspot(it) }
    }},
    modifier = Modifier.fillMaxSize()
)
```
`SceneView` from `com.google.android.filament.utils` + SceneView wrapper. Overlay dock/assistant via standard Compose on top.

3) Minimal assets (assets/filament/)
- `house_shell.glb` (12k verts, 180KB) â walls/floors/rooms
- `ibl_day.ktx` + `ibl_night.ktx` (small 64px cubemaps)
- `char_idle.glb` (2k verts, single 2s loop animation)
- No textures >256px; all vertex-colored or 1K atlas.

4) Day/night + idle (<1 day)
- Swap `IndirectLight` + `Skybox` on real-time clock (every 60s or on `onResume`).
- Single `glTFAnimator` on `char_idle` node, `playLoop("Idle")`.
- Speech reaction: scale `assistant_node` scale pulse via `TransformManager` on `AssistantHouseBridge` callback (no new assets).

5) Risk cut
If SceneView fails to compile: drop to raw Filament `FilamentView` + `ModelViewer` (remove SceneView dep), keep same `.glb` loading path. CityCanvas flag stays on.

6) Version
0.10.0-filament-m1
