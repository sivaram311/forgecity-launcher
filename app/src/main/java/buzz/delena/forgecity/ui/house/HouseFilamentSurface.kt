package buzz.delena.forgecity.ui.house

import android.view.MotionEvent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.android.filament.MaterialInstance
import io.github.sceneview.RenderQuality
import io.github.sceneview.SceneView
import io.github.sceneview.math.Direction
import io.github.sceneview.math.Position
import io.github.sceneview.math.Scale
import io.github.sceneview.math.Size
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberEnvironment
import io.github.sceneview.rememberEnvironmentLoader
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberModelLoader
import buzz.delena.forgecity.house.FilamentHouseLighting
import buzz.delena.forgecity.house.HouseWorld
import buzz.delena.forgecity.house.character.CharacterRole
import buzz.delena.forgecity.house.character.DefaultIdleHouseCharacters
import buzz.delena.forgecity.house.character.IdleHouseCharacter

private const val HOUSE_ASSET = "filament/house_shell.glb"
private const val CHAR_ASSET = "filament/char_idle.glb"

/**
 * Filament / SceneView 3D house HOME (0.10.0).
 *
 * Loads GLB shell + idle characters; day/night from [FilamentHouseLighting].
 * App markers are colored cubes; taps resolve via node [name] = building id.
 */
@Composable
fun HouseFilamentSurface(
    markers: List<HouseLabelMarker> = emptyList(),
    ambientEnabled: Boolean = true,
    allowsSoftShadows: Boolean = false,
    maxCharacters: Int = 3,
    assistantSpeaking: Boolean = false,
    night: Boolean = false,
    onMarkerTap: (HouseLabelMarker) -> Unit = {},
    onMarkerLongPress: (HouseLabelMarker) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val lighting = FilamentHouseLighting.forNight(night)
    val activeCharacters = remember(maxCharacters) {
        DefaultIdleHouseCharacters.take(maxCharacters.coerceAtLeast(0))
    }
    val markerById = remember(markers) { markers.associateBy { it.id } }

    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val environmentLoader = rememberEnvironmentLoader(engine)
    val environment = rememberEnvironment(environmentLoader)

    val cameraNode = rememberCameraNode(engine) {
        position = Position(x = 4.5f, y = 10.5f, z = 13.5f)
        lookAt(Position(x = 4.5f, y = 0.4f, z = 4.5f))
    }

    val mainLight = rememberMainLightNode(engine) {
        intensity = if (ambientEnabled) lighting.sunIntensity else lighting.sunIntensity * 0.35f
        lightDirection = Direction(lighting.dirX, lighting.dirY, lighting.dirZ)
        isShadowCaster = allowsSoftShadows
    }

    val houseInstance = rememberModelInstance(modelLoader, HOUSE_ASSET)

    Box(modifier = modifier.fillMaxSize()) {
        SceneView(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            environmentLoader = environmentLoader,
            environment = environment,
            mainLightNode = mainLight,
            fillLightNode = null,
            cameraNode = cameraNode,
            cameraManipulator = null,
            renderQuality = if (allowsSoftShadows) {
                RenderQuality.Default
            } else {
                RenderQuality.Performance
            },
            autoFitContent = false,
            isOpaque = true,
            onTouchEvent = { event, hit ->
                if (event.action != MotionEvent.ACTION_UP) return@SceneView false
                val id = hit?.node?.name ?: return@SceneView false
                val marker = markerById[id] ?: return@SceneView false
                if (event.eventTime - event.downTime > 450L) {
                    onMarkerLongPress(marker)
                } else {
                    onMarkerTap(marker)
                }
                true
            },
        ) {
            houseInstance?.let { instance ->
                ModelNode(
                    modelInstance = instance,
                    autoAnimate = false,
                    position = Position(x = 0f, y = 0f, z = 0f),
                )
            }

            markers.forEach { marker ->
                val room = HouseWorld.roomById(marker.roomId) ?: return@forEach
                val (x, y, z) = HouseWorld.positionInRoom(room, marker.nx, marker.ny, y = 0.85f)
                val mat = remember(marker.id) {
                    materialLoader.createColorInstance(
                        color = Color(0xFFE8A15A),
                        metallic = 0.15f,
                        roughness = 0.75f,
                    )
                }
                HotspotCube(
                    material = mat,
                    position = Position(x = x, y = y, z = z),
                    nodeName = marker.id,
                )
            }

            activeCharacters.forEach { character ->
                val room = HouseWorld.roomById(character.roomId) ?: return@forEach
                val (x, y, z) = HouseWorld.positionInRoom(room, character.nx, character.ny, y = 0f)
                val pulse =
                    if (character.role == CharacterRole.ASSISTANT && assistantSpeaking) {
                        lighting.speechPulseScale
                    } else {
                        1f
                    }
                CharacterModel(
                    assetPath = CHAR_ASSET,
                    position = Position(x = x, y = y, z = z),
                    scale = Scale(pulse),
                    nodeName = character.id,
                    fallbackTint = characterTint(character),
                )
            }
        }
    }
}

@Composable
private fun io.github.sceneview.SceneScope.HotspotCube(
    material: MaterialInstance,
    position: Position,
    nodeName: String,
) {
    CubeNode(
        size = Size(0.22f),
        materialInstance = material,
        position = position,
        apply = { name = nodeName },
    )
}

@Composable
private fun io.github.sceneview.SceneScope.CharacterModel(
    assetPath: String,
    position: Position,
    scale: Scale,
    nodeName: String,
    fallbackTint: Color,
) {
    val instance = rememberModelInstance(modelLoader, assetPath)
    if (instance != null) {
        ModelNode(
            modelInstance = instance,
            autoAnimate = false,
            position = position,
            scale = scale,
            apply = { name = nodeName },
        )
    } else {
        val mat = remember(nodeName) {
            materialLoader.createColorInstance(
                color = fallbackTint,
                metallic = 0.05f,
                roughness = 0.85f,
            )
        }
        CubeNode(
            size = Size(0.35f, 1.5f, 0.35f),
            materialInstance = mat,
            position = Position(x = position.x, y = position.y + 0.75f, z = position.z),
            scale = scale,
            apply = { name = nodeName },
        )
    }
}

private fun characterTint(character: IdleHouseCharacter): Color = when (character.role) {
    CharacterRole.MAYOR -> Color(0xFFC9A227)
    CharacterRole.ASSISTANT -> Color(0xFF6B9BD1)
    CharacterRole.NPC -> Color(0xFF8F6A3E)
}
