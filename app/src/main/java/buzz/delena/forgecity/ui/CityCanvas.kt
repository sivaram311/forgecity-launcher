package buzz.delena.forgecity.ui

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import buzz.delena.forgecity.city.CityBuilding
import buzz.delena.forgecity.city.District
import buzz.delena.forgecity.city.IsoMath
import buzz.delena.forgecity.city.IsoPoint
import kotlin.math.hypot

@Composable
fun CityCanvas(
    buildings: List<CityBuilding>,
    onBuildingTap: (CityBuilding) -> Unit,
    modifier: Modifier = Modifier,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val tileWidth = 88f
    val tileHeight = 44f

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.55f, 2.4f)
                    offset += pan
                }
            }
            .pointerInput(buildings, scale, offset) {
                detectTapGestures { tap ->
                    val cityPoint = Offset(
                        (tap.x - size.width / 2f - offset.x) / scale,
                        (tap.y - size.height * 0.28f - offset.y) / scale,
                    )
                    val hit = buildings.minByOrNull { building ->
                        val screen = IsoMath.gridToScreen(
                            building.col.toFloat(),
                            building.row.toFloat(),
                            tileWidth,
                            tileHeight,
                        )
                        hypot(cityPoint.x - screen.x, cityPoint.y - screen.y)
                    }
                    if (hit != null) {
                        val screen = IsoMath.gridToScreen(
                            hit.col.toFloat(),
                            hit.row.toFloat(),
                            tileWidth,
                            tileHeight,
                        )
                        if (hypot(cityPoint.x - screen.x, cityPoint.y - screen.y) < 42f) {
                            onBuildingTap(hit)
                        }
                    }
                }
            },
    ) {
        withTransform({
            translate(size.width / 2f + offset.x, size.height * 0.28f + offset.y)
            scale(scale, scale, pivot = Offset.Zero)
        }) {
            drawCircle(
                color = Color(0x2214121A),
                radius = 520f,
                center = Offset(0f, 180f),
            )
            buildings.forEach { building ->
                val point = IsoMath.gridToScreen(
                    building.col.toFloat(),
                    building.row.toFloat(),
                    tileWidth,
                    tileHeight,
                )
                drawBuildingPrism(point, districtColor(building.district), building.level)
                drawBuildingIcon(point, building.icon)
            }
        }
    }
}

private fun DrawScope.drawBuildingPrism(
    point: IsoPoint,
    color: Color,
    level: Int,
) {
    val height = 18f + level * 10f
    val halfW = 28f
    val halfH = 14f
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
}

private fun DrawScope.drawBuildingIcon(
    point: IsoPoint,
    icon: Drawable?,
) {
    if (icon == null) return
    val sizePx = 34.dp.toPx().toInt()
    val left = (point.x - sizePx / 2f).toInt()
    val top = (point.y - sizePx - 8f).toInt()
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
