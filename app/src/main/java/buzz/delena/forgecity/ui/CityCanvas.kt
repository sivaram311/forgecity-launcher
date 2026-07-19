package buzz.delena.forgecity.ui

import android.graphics.drawable.Drawable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import buzz.delena.forgecity.city.BuildingHitGeometry
import buzz.delena.forgecity.city.CityBuilding
import buzz.delena.forgecity.city.DayNightCycle
import buzz.delena.forgecity.city.District
import buzz.delena.forgecity.city.IsoLayout
import buzz.delena.forgecity.city.IsoMath
import buzz.delena.forgecity.city.IsoPoint
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.launch

@Composable
fun CityCanvas(
    buildings: List<CityBuilding>,
    hourOfDay: Int,
    ambientEnabled: Boolean,
    levelUpBuildingId: String?,
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
    val burst = remember { Animatable(0f) }
    var burstBuildingId by remember { mutableStateOf<String?>(null) }
    var pressedId by remember { mutableStateOf<String?>(null) }
    var growthId by remember { mutableStateOf<String?>(null) }
    val growth = remember { Animatable(1f) }
    var ambientPhase by remember { mutableFloatStateOf(0f) }

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

    LaunchedEffect(ambientEnabled) {
        if (!ambientEnabled) return@LaunchedEffect
        while (true) {
            withFrameMillis { frame ->
                ambientPhase = (frame % 4000L) / 4000f
            }
        }
    }

    LaunchedEffect(scale, offset) {
        if (!animatedScale.isRunning) {
            animatedScale.snapTo(scale)
            animatedOffset.snapTo(offset)
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.55f, 2.4f)
                    offset = (offset + pan).let {
                        Offset(it.x.coerceIn(-1200f, 1200f), it.y.coerceIn(-900f, 900f))
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
                        val hit = hitAt(tap.x, tap.y, canvasW, canvasH, buildings, animatedScale.value, animatedOffset.value)
                        if (hit != null) onBuildingLongPress(hit)
                    },
                    onPress = { tap ->
                        val canvasW = size.width.toFloat()
                        val canvasH = size.height.toFloat()
                        val hit = hitAt(tap.x, tap.y, canvasW, canvasH, buildings, animatedScale.value, animatedOffset.value)
                        pressedId = hit?.id
                        tryAwaitRelease()
                        pressedId = null
                    },
                    onTap = { tap ->
                        val canvasW = size.width.toFloat()
                        val canvasH = size.height.toFloat()
                        val hit = hitAt(tap.x, tap.y, canvasW, canvasH, buildings, animatedScale.value, animatedOffset.value)
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
                                val targetOffset = Offset(
                                    -screen.x * targetScale,
                                    -screen.y * targetScale + canvasH * 0.08f,
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
            if (ambientEnabled) {
                drawPowerGrid(ambientPhase)
            }
            drawCircle(
                color = Color(0x2214121A),
                radius = 620f,
                center = Offset(0f, 200f),
            )
            depthSorted.forEach { building ->
                val point = IsoMath.gridToScreen(
                    building.col.toFloat(),
                    building.row.toFloat(),
                    tileWidth,
                    tileHeight,
                )
                val heightScale = if (building.id == growthId) growth.value else 1f
                val pressed = building.id == pressedId
                drawBuildingPrism(
                    point = point,
                    color = districtColor(building.district),
                    level = building.level,
                    nightGlow = night && ambientEnabled,
                    heightScale = heightScale,
                    pressed = pressed,
                    favorite = building.isFavorite,
                )
                drawBuildingIcon(point, building.icon)
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

private fun DrawScope.drawPowerGrid(phase: Float) {
    val alpha = 0.18f + 0.08f * sin(phase * Math.PI.toFloat() * 2f)
    val color = Color(0xFF7C5CFF).copy(alpha = alpha)
    val stroke = Stroke(width = 1.4f, cap = StrokeCap.Round)
    for (i in -4..4) {
        val a = IsoMath.gridToScreen(i * 2f, -4f, IsoLayout.TILE_WIDTH, IsoLayout.TILE_HEIGHT)
        val b = IsoMath.gridToScreen(i * 2f, 8f, IsoLayout.TILE_WIDTH, IsoLayout.TILE_HEIGHT)
        drawLine(color, Offset(a.x, a.y), Offset(b.x, b.y), strokeWidth = stroke.width)
        val c = IsoMath.gridToScreen(-4f, i * 2f, IsoLayout.TILE_WIDTH, IsoLayout.TILE_HEIGHT)
        val d = IsoMath.gridToScreen(8f, i * 2f, IsoLayout.TILE_WIDTH, IsoLayout.TILE_HEIGHT)
        drawLine(color, Offset(c.x, c.y), Offset(d.x, d.y), strokeWidth = stroke.width)
    }
}

private fun DrawScope.drawLevelUpBurst(
    point: IsoPoint,
    level: Int,
    progress: Float,
) {
    val roofTop = Offset(point.x, point.y - BuildingHitGeometry.prismHeight(level))
    val fade = (1f - progress).coerceIn(0f, 1f)
    val particleCount = 12
    val spread = 26f + progress * 46f
    repeat(particleCount) { i ->
        val angle = (2.0 * Math.PI * i / particleCount).toFloat()
        val center = Offset(
            roofTop.x + cos(angle) * spread,
            roofTop.y + sin(angle) * spread * 0.6f - progress * 18f,
        )
        drawCircle(
            color = Color(0xFFFFE08A).copy(alpha = fade),
            radius = 3.5f * fade + 1.5f,
            center = center,
        )
    }
    drawCircle(
        color = Color(0xFFFFF3C4).copy(alpha = fade * 0.5f),
        radius = spread,
        center = roofTop,
    )
}

private fun DrawScope.drawBuildingPrism(
    point: IsoPoint,
    color: Color,
    level: Int,
    nightGlow: Boolean,
    heightScale: Float,
    pressed: Boolean,
    favorite: Boolean,
) {
    val height = BuildingHitGeometry.prismHeight(level) * heightScale
    val halfW = IsoLayout.HALF_W * if (pressed) 1.08f else 1f
    val halfH = IsoLayout.HALF_H * if (pressed) 1.08f else 1f
    val base = Path().apply {
        moveTo(point.x, point.y)
        lineTo(point.x + halfW, point.y + halfH)
        lineTo(point.x, point.y + halfH * 2)
        lineTo(point.x - halfW, point.y + halfH)
        close()
    }
    drawPath(base, color.copy(alpha = 0.55f))
    drawPath(base, Color.White.copy(alpha = 0.18f), style = Stroke(width = 1.5f))

    val leftWall = Path().apply {
        moveTo(point.x - halfW, point.y + halfH)
        lineTo(point.x, point.y + halfH * 2)
        lineTo(point.x, point.y + halfH * 2 - height)
        lineTo(point.x - halfW, point.y + halfH - height)
        close()
    }
    drawPath(leftWall, color.copy(alpha = 0.75f))

    val rightWall = Path().apply {
        moveTo(point.x + halfW, point.y + halfH)
        lineTo(point.x, point.y + halfH * 2)
        lineTo(point.x, point.y + halfH * 2 - height)
        lineTo(point.x + halfW, point.y + halfH - height)
        close()
    }
    drawPath(rightWall, color.copy(alpha = 0.92f))

    val roof = Path().apply {
        moveTo(point.x, point.y - height)
        lineTo(point.x + halfW, point.y + halfH - height)
        lineTo(point.x, point.y + halfH * 2 - height)
        lineTo(point.x - halfW, point.y + halfH - height)
        close()
    }
    drawPath(roof, color)

    if (nightGlow) {
        drawCircle(
            color = Color(0x66FFE08A),
            radius = 10f + level * 2f,
            center = Offset(point.x, point.y - height * 0.35f),
        )
    }
    if (pressed || favorite) {
        drawCircle(
            color = Color(0x88FF9A4A),
            radius = if (pressed) 22f else 14f,
            center = Offset(point.x, point.y - height * 0.2f),
        )
    }
}

private fun DrawScope.drawBuildingIcon(
    point: IsoPoint,
    icon: Drawable?,
) {
    if (icon == null) return
    val sizePx = 36.dp.toPx().toInt()
    val left = (point.x - sizePx / 2f).toInt()
    val top = (point.y - sizePx - 10f).toInt()
    icon.setBounds(left, top, left + sizePx, top + sizePx)
    icon.draw(drawContext.canvas.nativeCanvas)
}

private fun districtColor(district: District): Color = when (district) {
    District.FORGE -> Color(0xFFE8A15A)
    District.VAULT -> Color(0xFFD4AF37)
    District.NEXUS -> Color(0xFF4FD1C5)
    District.ARENA -> Color(0xFFE5738A)
    District.GARDEN -> Color(0xFF7CB342)
    District.ARCHIVE -> Color(0xFF90A4AE)
    District.CUSTOM -> Color(0xFFB39DDB)
}
