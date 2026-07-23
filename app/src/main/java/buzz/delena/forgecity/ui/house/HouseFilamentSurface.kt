package buzz.delena.forgecity.ui.house

import android.view.MotionEvent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.filament.LightManager
import com.google.android.filament.MaterialInstance
import io.github.sceneview.RenderQuality
import io.github.sceneview.SceneView
import io.github.sceneview.SurfaceType
import io.github.sceneview.math.Direction
import io.github.sceneview.math.Position
import io.github.sceneview.math.Scale
import io.github.sceneview.math.Size
import io.github.sceneview.math.colorOf
import io.github.sceneview.rememberCameraManipulator
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberEnvironment
import io.github.sceneview.rememberEnvironmentLoader
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberView
import buzz.delena.forgecity.house.DustMoteCloud
import buzz.delena.forgecity.house.FilamentHouseLighting
import buzz.delena.forgecity.house.HouseWorld
import buzz.delena.forgecity.house.character.CharacterRole
import buzz.delena.forgecity.house.character.DefaultIdleHouseCharacters
import buzz.delena.forgecity.house.character.IdleHouseCharacter
import kotlin.math.roundToInt

private const val HOUSE_ASSET = "filament/house_shell.glb"
private const val CHAR_ASSET = "filament/char_idle.glb"

/**
 * Filament house HOME — 0.10.4 white-screen exposure fix (Grok).
 *
 * Bare setExposure(1.x) EV blew the frame white while orbit still worked.
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
    val dustCount = DustMoteCloud.countFor(allowsSoftShadows, ambientEnabled)
    val dustSeeds = remember(dustCount) { DustMoteCloud.seeds(dustCount) }
    var timeSec by remember { mutableFloatStateOf(0f) }

    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val view = rememberView(engine)
    val environmentLoader = rememberEnvironmentLoader(engine)

    val environment = rememberEnvironment(environmentLoader)
    SideEffect {
        environment.indirectLight?.intensity = lighting.iblIntensity
    }

    val cameraNode = rememberCameraNode(engine) {
        position = Position(x = 4.5f, y = 9.2f, z = 12.2f)
        lookAt(Position(x = 4.5f, y = 0.6f, z = 4.5f))
        // Photographic exposure — NEVER bare EV float (1.15 blew the frame to white).
        setExposure(lighting.aperture, lighting.shutterSpeed, lighting.iso)
    }
    SideEffect {
        cameraNode.setExposure(lighting.aperture, lighting.shutterSpeed, lighting.iso)
        runCatching {
            environment.skybox?.setColor(
                floatArrayOf(lighting.skyTopR, lighting.skyTopG, lighting.skyTopB, 1f),
            )
        }
    }

    val cameraManipulator = rememberCameraManipulator(
        orbitHomePosition = cameraNode.worldPosition,
        targetPosition = Position(x = 4.5f, y = 0.6f, z = 4.5f),
    )

    val mainLight = rememberMainLightNode(engine) {
        intensity = if (ambientEnabled) lighting.sunIntensity else lighting.sunIntensity * 0.35f
        lightDirection = Direction(lighting.dirX, lighting.dirY, lighting.dirZ)
        color = colorOf(
            if (night) Color(0xFFB8C4E0) else Color(0xFFFFF2DC),
        )
        isShadowCaster = allowsSoftShadows
    }

    SideEffect {
        runCatching { view.setShadowingEnabled(allowsSoftShadows) }
        try {
            view.fogOptions = view.fogOptions.apply {
                enabled = ambientEnabled
                density = lighting.fogDensity
                distance = 2.5f
                maximumOpacity = if (night) 0.55f else 0.40f
                color[0] = lighting.fogColorR
                color[1] = lighting.fogColorG
                color[2] = lighting.fogColorB
            }
        } catch (_: Throwable) {
        }
        try {
            view.bloomOptions = view.bloomOptions.apply {
                enabled = ambientEnabled && lighting.bloomEnabled
                strength = lighting.bloomStrength
            }
        } catch (_: Throwable) {
        }
    }

    val houseInstance = rememberModelInstance(modelLoader, HOUSE_ASSET)
    val quality = when {
        allowsSoftShadows -> RenderQuality.Default
        else -> RenderQuality.Performance
    }
    val fillPulse = DustMoteCloud.windowPulse(timeSec)
    val fillIntensity =
        (if (ambientEnabled) lighting.fillIntensity else lighting.fillIntensity * 0.3f) * fillPulse

    Box(modifier = modifier.fillMaxSize()) {
        SceneView(
            modifier = Modifier.fillMaxSize(),
            surfaceType = SurfaceType.TextureSurface,
            engine = engine,
            modelLoader = modelLoader,
            view = view,
            environment = environment,
            mainLightNode = mainLight,
            fillLightNode = null,
            cameraNode = cameraNode,
            cameraManipulator = cameraManipulator,
            renderQuality = quality,
            autoCenterContent = false,
            autoFitContent = false,
            isOpaque = true,
            onFrame = { nanos ->
                val t = nanos / 1_000_000_000f
                // Throttle dust/window pulse updates ~30 Hz to limit Compose churn on Adreno 710.
                if (t - timeSec >= 0.033f) timeSec = t
            },
            onTouchEvent = { event, hit ->
                if (event.action != MotionEvent.ACTION_UP) return@SceneView false
                val id = hit?.node?.name ?: return@SceneView false
                val marker = markers.firstOrNull { it.id == id } ?: return@SceneView false
                if (event.eventTime - event.downTime > 450L) {
                    onMarkerLongPress(marker)
                } else {
                    onMarkerTap(marker)
                }
                true
            },
        ) {
            LightNode(
                type = LightManager.Type.DIRECTIONAL,
                intensity = fillIntensity,
                direction = Direction(lighting.fillDirX, lighting.fillDirY, lighting.fillDirZ),
                color = colorOf(if (night) Color(0xFFFFC978) else Color(0xFFFFE0B0)),
            )

            houseInstance?.let { instance ->
                ModelNode(
                    modelInstance = instance,
                    autoAnimate = false,
                    position = Position(x = 0f, y = 0f, z = 0f),
                )
            }

            val dustMat = remember {
                materialLoader.createColorInstance(
                    color = Color(0x66FFF6E8),
                    metallic = 0f,
                    roughness = 1f,
                )
            }
            dustSeeds.forEachIndexed { index, mote ->
                val (x, y, z) = DustMoteCloud.positionAt(mote, timeSec)
                CubeNode(
                    size = Size(0.035f),
                    materialInstance = dustMat,
                    position = Position(x = x, y = y, z = z),
                    apply = { name = "dust_$index" },
                )
            }

            markers.forEach { marker ->
                val room = HouseWorld.roomById(marker.roomId) ?: return@forEach
                val (x, y, z) = HouseWorld.positionInRoom(room, marker.nx, marker.ny, y = 0.85f)
                val mat = remember(marker.id) {
                    materialLoader.createColorInstance(
                        color = Color(0xFFE8A15A),
                        metallic = 0.12f,
                        roughness = 0.72f,
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

        HouseMarkerChips(
            markers = markers,
            onMarkerTap = onMarkerTap,
            onMarkerLongPress = onMarkerLongPress,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HouseMarkerChips(
    markers: List<HouseLabelMarker>,
    onMarkerTap: (HouseLabelMarker) -> Unit,
    onMarkerLongPress: (HouseLabelMarker) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    BoxWithConstraints(modifier = modifier) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val layout = remember(widthPx, heightPx) { houseLayoutForHit(widthPx, heightPx) }
        markers.forEach { marker ->
            val cell = layout.cellById(marker.roomId) ?: return@forEach
            val cx = cell.left + cell.width * marker.nx.coerceIn(0.12f, 0.88f)
            val cy = cell.top + cell.height * marker.ny.coerceIn(0.18f, 0.82f)
            val chipPx = minOf(cell.width, cell.height) * 0.22f
            val chipDp = with(density) { chipPx.toDp() }.coerceAtLeast(36.dp)
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (cx - chipPx / 2f).roundToInt(),
                            (cy - chipPx / 2f).roundToInt(),
                        )
                    }
                    .size(chipDp)
                    .clip(CircleShape)
                    .combinedClickable(
                        onClick = { onMarkerTap(marker) },
                        onLongClick = { onMarkerLongPress(marker) },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = marker.label.take(10),
                    color = Color(0xFFF6E7D0),
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private data class HitLayout(
    val origin: Offset,
    val mainW: Float,
    val cellW: Float,
    val cellH: Float,
    val vaultW: Float,
) {
    fun cellById(id: String): Rect? {
        val (col, row) = when (id) {
            "kitchen" -> 0 to 0
            "living" -> 1 to 0
            "hallway" -> 0 to 1
            "office" -> 1 to 1
            "bedroom" -> 0 to 2
            "workshop" -> 1 to 2
            "vault" -> 2 to 1
            else -> return null
        }
        if (col == 2) {
            val left = origin.x + mainW
            val top = origin.y + cellH
            return Rect(left, top, left + vaultW, top + cellH)
        }
        val left = origin.x + col * cellW
        val top = origin.y + row * cellH
        return Rect(left, top, left + cellW, top + cellH)
    }
}

private fun houseLayoutForHit(width: Float, height: Float): HitLayout {
    val padX = width * 0.06f
    val padY = height * 0.10f
    val houseW = width - padX * 2f
    val houseH = height - padY * 2f
    val vaultW = houseW * 0.22f
    val mainW = houseW - vaultW
    return HitLayout(
        origin = Offset(padX, padY),
        mainW = mainW,
        cellW = mainW / 2f,
        cellH = houseH / 3f,
        vaultW = vaultW,
    )
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
