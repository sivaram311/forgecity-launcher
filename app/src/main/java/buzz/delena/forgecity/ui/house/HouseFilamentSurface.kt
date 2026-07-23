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
import io.github.sceneview.math.Size
import io.github.sceneview.math.colorOf
import io.github.sceneview.rememberCameraManipulator
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.createEnvironment
import io.github.sceneview.rememberEnvironment
import io.github.sceneview.rememberEnvironmentLoader
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberView
import buzz.delena.forgecity.house.DustMoteCloud
import buzz.delena.forgecity.house.FilamentDayCycle
import buzz.delena.forgecity.house.FilamentHouseIbl
import buzz.delena.forgecity.house.FilamentHouseLighting
import buzz.delena.forgecity.house.HouseFacadeFinishing
import buzz.delena.forgecity.house.HouseWorld
import buzz.delena.forgecity.house.character.DefaultIdleHouseCharacters
import buzz.delena.forgecity.house.character.HouseCharacterMotion
import buzz.delena.forgecity.house.character.HouseFaceAssets
import buzz.delena.forgecity.house.character.HouseHumanoidPose
import io.github.sceneview.texture.ImageTexture
import kotlin.math.roundToInt

private const val HOUSE_ASSET = "filament/house_shell.glb"

/**
 * Filament house HOME — 0.14 HDR IBL + reflectance fresnel stand-ins; open-roof + patrols.
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
    val baseLighting = FilamentHouseLighting.forNight(night)
    val activeCharacters = remember(maxCharacters) {
        DefaultIdleHouseCharacters.take(maxCharacters.coerceAtLeast(0))
    }
    val dustCount = DustMoteCloud.countFor(allowsSoftShadows, ambientEnabled)
    val dustSeeds = remember(dustCount) { DustMoteCloud.seeds(dustCount) }
    var timeSec by remember { mutableFloatStateOf(0f) }

    val daySample = if (!night) {
        FilamentDayCycle.sample(FilamentDayCycle.tDay(timeSec))
    } else {
        null
    }

    val sunIntensity = when {
        !ambientEnabled -> (daySample?.sunIntensity ?: baseLighting.sunIntensity) * 0.35f
        daySample != null -> daySample.sunIntensity
        else -> baseLighting.sunIntensity
    }
    val fillBase = daySample?.fillIntensity ?: baseLighting.fillIntensity
    val fillPulse = DustMoteCloud.windowPulse(timeSec)
    val emissivePulse = HouseFacadeFinishing.emissivePulse(timeSec)
    val fillIntensity =
        (if (ambientEnabled) fillBase else fillBase * 0.3f) * fillPulse * emissivePulse

    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val view = rememberView(engine)
    val environmentLoader = rememberEnvironmentLoader(engine)

    val environment = rememberEnvironment(
        environmentLoader = environmentLoader,
        environment = remember(environmentLoader) {
            {
                val hdr = runCatching {
                    environmentLoader.createHDREnvironment(FilamentHouseIbl.HDR_ASSET)
                }.getOrNull()
                hdr ?: createEnvironment(environmentLoader, isOpaque = true)
            }
        },
    )
    SideEffect {
        environment.indirectLight?.intensity =
            FilamentHouseIbl.iblIntensity(night, daySample?.hemiIntensity)
    }

    val cameraNode = rememberCameraNode(engine) {
        // High dollhouse orbit — clear of wall tops; open roof shows interiors.
        position = Position(x = 4.5f, y = 11.5f, z = 13.5f)
        lookAt(Position(x = 4.5f, y = 0.35f, z = 4.2f))
        setExposure(baseLighting.aperture, baseLighting.shutterSpeed, baseLighting.iso)
    }
    SideEffect {
        cameraNode.setExposure(baseLighting.aperture, baseLighting.shutterSpeed, baseLighting.iso)
        val skyR = daySample?.skyR ?: baseLighting.skyTopR
        val skyG = daySample?.skyG ?: baseLighting.skyTopG
        val skyB = daySample?.skyB ?: baseLighting.skyTopB
        runCatching {
            environment.skybox?.setColor(floatArrayOf(skyR, skyG, skyB, 1f))
        }
    }

    val cameraManipulator = rememberCameraManipulator(
        orbitHomePosition = cameraNode.worldPosition,
        targetPosition = Position(x = 4.5f, y = 0.35f, z = 4.2f),
    )

    val sunColor = daySample?.sunColor ?: if (night) Color(0xFFB8C4E0) else Color(0xFFFFF2DC)
    val sunDir = if (daySample != null) {
        Direction(daySample.dirX, daySample.dirY, daySample.dirZ)
    } else {
        Direction(baseLighting.dirX, baseLighting.dirY, baseLighting.dirZ)
    }

    val mainLight = rememberMainLightNode(engine) {
        intensity = sunIntensity
        lightDirection = sunDir
        color = colorOf(sunColor)
        isShadowCaster = allowsSoftShadows
    }
    SideEffect {
        mainLight.intensity = sunIntensity
        mainLight.lightDirection = sunDir
        mainLight.color = colorOf(sunColor)
        mainLight.isShadowCaster = allowsSoftShadows
    }

    SideEffect {
        runCatching { view.setShadowingEnabled(allowsSoftShadows) }
        try {
            view.fogOptions = view.fogOptions.apply {
                enabled = ambientEnabled
                density = baseLighting.fogDensity
                distance = 2.5f
                maximumOpacity = if (night) 0.55f else 0.40f
                color[0] = baseLighting.fogColorR
                color[1] = baseLighting.fogColorG
                color[2] = baseLighting.fogColorB
            }
        } catch (_: Throwable) {
        }
        try {
            view.bloomOptions = view.bloomOptions.apply {
                enabled = ambientEnabled && baseLighting.bloomEnabled
                strength = baseLighting.bloomStrength
            }
        } catch (_: Throwable) {
        }
    }

    val houseInstance = rememberModelInstance(modelLoader, HOUSE_ASSET)
    val quality = when {
        allowsSoftShadows -> RenderQuality.Default
        else -> RenderQuality.Performance
    }
    val fillColor = daySample?.fillColor ?: if (night) Color(0xFFFFC978) else Color(0xFFFFE0B0)
    val fillDir = if (daySample != null) {
        Direction(-daySample.dirX * 0.6f, -0.35f, -daySample.dirZ * 0.6f)
    } else {
        Direction(baseLighting.fillDirX, baseLighting.fillDirY, baseLighting.fillDirZ)
    }

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
                direction = fillDir,
                color = colorOf(fillColor),
            )
            // Soft sky bounce (hemisphere stand-in — Filament has no Three.js HemisphereLight).
            if (daySample != null && ambientEnabled) {
                LightNode(
                    type = LightManager.Type.DIRECTIONAL,
                    intensity = daySample.hemiIntensity * 0.35f,
                    direction = Direction(0.15f, -1f, 0.1f),
                    color = colorOf(daySample.hemiSky),
                )
            }
            // Grazing rim (PH fresnel stand-in) — Adreno-safe directional, not custom filamat.
            LightNode(
                type = LightManager.Type.DIRECTIONAL,
                intensity = FilamentHouseIbl.rimIntensity(ambientEnabled, night, emissivePulse),
                direction = Direction(-0.82f, -0.12f, 0.42f),
                color = colorOf(if (night) Color(0xFF7EC8E3) else Color(0xFFE8D4B8)),
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
                    color = Color(0x55FFF6E8),
                    metallic = 0f,
                    roughness = 1f,
                )
            }
            // One shared face texture for all humanoids (assets/faces/siva.png).
            val sharedFaceMat = remember {
                runCatching {
                    val tex = ImageTexture.Builder()
                        .bitmap(materialLoader.context.assets, HouseFaceAssets.SHARED_FACE)
                        .build(engine)
                    materialLoader.createTextureInstance(
                        texture = tex,
                        isOpaque = false,
                        metallic = 0f,
                        roughness = 0.55f,
                        reflectance = 0.04f,
                    )
                }.getOrNull()
            }
            val dustRadius = DustMoteCloud.radiusFor(allowsSoftShadows)
            dustSeeds.forEachIndexed { index, mote ->
                val (x, y, z) = DustMoteCloud.positionAt(mote, timeSec)
                SphereNode(
                    radius = dustRadius,
                    materialInstance = dustMat,
                    position = Position(x = x, y = y, z = z),
                    apply = { name = "dust_$index" },
                )
            }

            // Window emissive planes + corner rim strips (pulse via fill light + slight Y scale).
            if (ambientEnabled) {
                val windowScale = 0.92f + 0.12f * emissivePulse
                HouseFacadeFinishing.windowPanes.forEachIndexed { index, pane ->
                    val warm = if (pane.warm) Color(0xAAE8B86D) else Color(0xAA7EC8E3)
                    val mat = remember(index, pane.warm) {
                        // Lit glass: IBL + reflectance for fresnel edges (gap #8).
                        materialLoader.createColorInstance(
                            color = warm,
                            metallic = 0.12f,
                            roughness = 0.22f,
                            reflectance = FilamentHouseIbl.GLASS_REFLECTANCE,
                        )
                    }
                    CubeNode(
                        size = Size(pane.sx, pane.sy * windowScale, pane.sz),
                        materialInstance = mat,
                        position = Position(pane.x, pane.y, pane.z),
                        apply = { name = "window_$index" },
                    )
                }
                HouseFacadeFinishing.cornerRims.forEachIndexed { index, strip ->
                    val base = if (strip.cool) Color(0x887EC8E3) else Color(0x88E8B86D)
                    val mat = remember(index, strip.cool) {
                        materialLoader.createColorInstance(
                            color = base,
                            metallic = 0.08f,
                            roughness = 0.30f,
                            reflectance = FilamentHouseIbl.RIM_STRIP_REFLECTANCE,
                        )
                    }
                    CubeNode(
                        size = Size(strip.sx, strip.sy, strip.sz),
                        materialInstance = mat,
                        position = Position(strip.x, strip.y, strip.z),
                        apply = { name = "rim_$index" },
                    )
                }
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
                val motion = HouseCharacterMotion.sample(character, timeSec, assistantSpeaking)
                val (x, _, z) = HouseWorld.positionInRoom(room, motion.nx, motion.nz, y = 0f)
                val pose = HouseHumanoidPose.compute(
                    action = motion.action,
                    timeSec = timeSec,
                    phase = character.phaseOffset * 6.28f,
                )
                HouseHumanoidNode(
                    look = HouseHumanoidPose.lookFor(character.role),
                    pose = pose,
                    worldPosition = Position(x = x, y = motion.y, z = z),
                    nodeName = character.id,
                    yawDeg = motion.yawDeg,
                    faceMaterial = sharedFaceMat,
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
