package buzz.delena.forgecity.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntSize
import android.view.HapticFeedbackConstants
import buzz.delena.forgecity.city.BuildingHitGeometry
import buzz.delena.forgecity.city.CityBuilding
import buzz.delena.forgecity.city.DayNightCycle
import buzz.delena.forgecity.city.IsoLayout
import buzz.delena.forgecity.city.IsoMath
import buzz.delena.forgecity.ui.cityrender.drawCityBuilding
import buzz.delena.forgecity.ui.cityrender.drawGroundPlane
import buzz.delena.forgecity.ui.cityrender.drawLevelUpBurst
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.launch

@Composable
fun CityCanvas(
    buildings: List<CityBuilding>,
    hourOfDay: Int,
    ambientEnabled: Boolean,
    levelUpBuildingId: String?,
    focusBuildingId: String? = null,
    onBuildingTap: (CityBuilding) -> Unit,
    onBuildingLongPress: (CityBuilding) -> Unit,
    onLevelUpConsumed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val animatedScale = remember { Animatable(1f) }
    val animatedOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val scope = rememberCoroutineScope()
    val view = LocalView.current
    val burst = remember { Animatable(0f) }
    var burstBuildingId by remember { mutableStateOf<String?>(null) }
    var pressedId by remember { mutableStateOf<String?>(null) }
    var growthId by remember { mutableStateOf<String?>(null) }
    val growth = remember { Animatable(1f) }
    var ambientPhase by remember { mutableFloatStateOf(0f) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    val tileWidth = IsoLayout.TILE_WIDTH
    val tileHeight = IsoLayout.TILE_HEIGHT
    val density = LocalDensity.current
    val night = DayNightCycle.isNight(hourOfDay)
    val starAlpha = DayNightCycle.starAlpha(hourOfDay)
    val stars = remember {
        List(36) {
            Offset(Random.nextFloat(), Random.nextFloat() * 0.55f) to (0.4f + Random.nextFloat() * 0.6f)
        }
    }
    val depthSorted = remember(buildings) {
        buildings.sortedBy { BuildingHitGeometry.depthKey(it.col, it.row) }
    }

    fun clampOffset(value: Offset): Offset =
        Offset(value.x.coerceIn(-1200f, 1200f), value.y.coerceIn(-900f, 900f))

    fun clampScale(value: Float): Float = value.coerceIn(0.55f, 2.4f)

    LaunchedEffect(levelUpBuildingId) {
        val id = levelUpBuildingId ?: return@LaunchedEffect
        growthId = id
        growth.snapTo(0.55f)
        growth.animateTo(1f, tween(480))
        burstBuildingId = id
        burst.snapTo(0f)
        burst.animateTo(1f, tween(700))
        burstBuildingId = null
        growthId = null
        onLevelUpConsumed()
    }

    // Slice D2: fly camera to the sole search match.
    LaunchedEffect(focusBuildingId, buildings, canvasSize) {
        val id = focusBuildingId ?: return@LaunchedEffect
        if (canvasSize.width == 0) return@LaunchedEffect
        val target = buildings.firstOrNull { it.id == id } ?: return@LaunchedEffect
        val screen = IsoMath.gridToScreen(
            target.col.toFloat(),
            target.row.toFloat(),
            tileWidth,
            tileHeight,
        )
        val targetScale = 1.65f
        val targetOffset = clampOffset(
            Offset(
                -screen.x * targetScale,
                -screen.y * targetScale + canvasSize.height * 0.08f,
            ),
        )
        scale = targetScale
        offset = targetOffset
        animatedScale.animateTo(targetScale, tween(420))
        animatedOffset.animateTo(targetOffset, tween(420))
    }

    LaunchedEffect(ambientEnabled) {
        if (!ambientEnabled) return@LaunchedEffect
        while (true) {
            withFrameMillis { frame ->
                ambientPhase = (frame % 4000L) / 4000f
            }
        }
    }

    LaunchedEffect(scale, offset) {
        if (!animatedScale.isRunning && !animatedOffset.isRunning) {
            animatedScale.snapTo(scale)
            animatedOffset.snapTo(offset)
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { canvasSize = it }
            .pointerInput(Unit) {
                while (true) {
                    var lastTimeNs = 0L
                    var velocity = Offset.Zero
                    detectTransformGestures { _, pan, zoom, _ ->
                        val now = System.nanoTime()
                        if (lastTimeNs > 0L) {
                            val dtSec = (now - lastTimeNs) / 1_000_000_000f
                            if (dtSec in 0.001f..0.08f) {
                                velocity = Offset(pan.x / dtSec, pan.y / dtSec)
                            }
                        }
                        lastTimeNs = now
                        scale = clampScale(scale * zoom)
                        offset = clampOffset(offset + pan)
                        scope.launch {
                            animatedScale.snapTo(scale)
                            animatedOffset.snapTo(offset)
                        }
                    }
                    // Slice D1: pan inertia after the gesture ends.
                    val speed = hypot(velocity.x.toDouble(), velocity.y.toDouble()).toFloat()
                    if (speed > 280f) {
                        val decay = exponentialDecay<Offset>(frictionMultiplier = 1.35f)
                        val start = offset
                        animatedOffset.snapTo(start)
                        val result = animatedOffset.animateDecay(velocity, decay)
                        val end = clampOffset(result.endState.value)
                        offset = end
                        if (end != result.endState.value) {
                            animatedOffset.snapTo(end)
                        }
                    }
                }
            }
            .pointerInput(buildings, scale, offset) {
                detectTapGestures(
                    onDoubleTap = {
                        scope.launch {
                            scale = 1f
                            offset = Offset.Zero
                            animatedScale.animateTo(1f, tween(320))
                            animatedOffset.animateTo(Offset.Zero, tween(320))
                        }
                    },
                    onLongPress = { tap ->
                        val canvasW = size.width.toFloat()
                        val canvasH = size.height.toFloat()
                        val hit = hitAt(
                            tap.x,
                            tap.y,
                            canvasW,
                            canvasH,
                            buildings,
                            animatedScale.value,
                            animatedOffset.value,
                        )
                        if (hit != null) {
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            onBuildingLongPress(hit)
                        }
                    },
                    onPress = { tap ->
                        val canvasW = size.width.toFloat()
                        val canvasH = size.height.toFloat()
                        val hit = hitAt(
                            tap.x,
                            tap.y,
                            canvasW,
                            canvasH,
                            buildings,
                            animatedScale.value,
                            animatedOffset.value,
                        )
                        pressedId = hit?.id
                        tryAwaitRelease()
                        pressedId = null
                    },
                    onTap = { tap ->
                        val canvasW = size.width.toFloat()
                        val canvasH = size.height.toFloat()
                        val hit = hitAt(
                            tap.x,
                            tap.y,
                            canvasW,
                            canvasH,
                            buildings,
                            animatedScale.value,
                            animatedOffset.value,
                        )
                        if (hit != null) {
                            val screen = IsoMath.gridToScreen(
                                hit.col.toFloat(),
                                hit.row.toFloat(),
                                tileWidth,
                                tileHeight,
                            )
                            scope.launch {
                                pressedId = hit.id
                                val targetScale = 1.75f
                                val targetOffset = clampOffset(
                                    Offset(
                                        -screen.x * targetScale,
                                        -screen.y * targetScale + canvasH * 0.08f,
                                    ),
                                )
                                animatedScale.animateTo(targetScale, tween(360))
                                animatedOffset.animateTo(targetOffset, tween(360))
                                scale = targetScale
                                offset = targetOffset
                                pressedId = null
                                onBuildingTap(hit)
                            }
                        }
                    },
                )
            },
    ) {
        val parallax = animatedOffset.value * 0.35f
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color(0x22182030), Color(0x00000000)),
            ),
            topLeft = Offset(parallax.x - size.width * 0.1f, parallax.y),
            size = androidx.compose.ui.geometry.Size(size.width * 1.2f, size.height * 0.55f),
        )

        if (ambientEnabled && starAlpha > 0f) {
            stars.forEach { (norm, brightness) ->
                drawCircle(
                    color = Color.White.copy(alpha = starAlpha * brightness * 0.7f),
                    radius = 1.6f * density.density,
                    center = Offset(norm.x * size.width + parallax.x * 0.2f, norm.y * size.height),
                )
            }
        }

        withTransform({
            translate(size.width / 2f + animatedOffset.value.x, size.height * 0.28f + animatedOffset.value.y)
            scale(animatedScale.value, animatedScale.value, pivot = Offset.Zero)
        }) {
            drawGroundPlane(ambientEnabled)
            if (ambientEnabled) {
                drawAmbientPowerGrid(ambientPhase)
            }
            depthSorted.forEach { building ->
                val point = IsoMath.gridToScreen(
                    building.col.toFloat(),
                    building.row.toFloat(),
                    tileWidth,
                    tileHeight,
                )
                val heightScale = if (building.id == growthId) growth.value else 1f
                drawCityBuilding(
                    point = point,
                    district = building.district,
                    level = building.level,
                    nightGlow = night && ambientEnabled,
                    heightScale = heightScale,
                    pressed = building.id == pressedId,
                    favorite = building.isFavorite,
                    scale = animatedScale.value,
                    icon = building.icon,
                )
            }

            val celebrating = burstBuildingId
            if (celebrating != null) {
                depthSorted.firstOrNull { it.id == celebrating }?.let { building ->
                    val point = IsoMath.gridToScreen(
                        building.col.toFloat(),
                        building.row.toFloat(),
                        tileWidth,
                        tileHeight,
                    )
                    drawLevelUpBurst(point, building.level, burst.value)
                }
            }
        }
    }
}

private fun hitAt(
    tapX: Float,
    tapY: Float,
    width: Float,
    height: Float,
    buildings: List<CityBuilding>,
    currentScale: Float,
    currentOffset: Offset,
): CityBuilding? {
    val cityX = (tapX - width / 2f - currentOffset.x) / currentScale
    val cityY = (tapY - height * 0.28f - currentOffset.y) / currentScale
    return BuildingHitGeometry.pickBuilding(
        cityX = cityX,
        cityY = cityY,
        buildings = buildings,
        colOf = { it.col },
        rowOf = { it.row },
        levelOf = { it.level },
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawAmbientPowerGrid(phase: Float) {
    val alpha = 0.12f + 0.06f * sin(phase * Math.PI.toFloat() * 2f)
    val color = Color(0xFF7C5CFF).copy(alpha = alpha)
    for (i in -4..4) {
        val a = IsoMath.gridToScreen(i * 2f, -4f, IsoLayout.TILE_WIDTH, IsoLayout.TILE_HEIGHT)
        val b = IsoMath.gridToScreen(i * 2f, 8f, IsoLayout.TILE_WIDTH, IsoLayout.TILE_HEIGHT)
        drawLine(color, Offset(a.x, a.y), Offset(b.x, b.y), strokeWidth = 1.2f)
        val c = IsoMath.gridToScreen(-4f, i * 2f, IsoLayout.TILE_WIDTH, IsoLayout.TILE_HEIGHT)
        val d = IsoMath.gridToScreen(8f, i * 2f, IsoLayout.TILE_WIDTH, IsoLayout.TILE_HEIGHT)
        drawLine(color, Offset(c.x, c.y), Offset(d.x, d.y), strokeWidth = 1.2f)
    }
}
