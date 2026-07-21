package buzz.delena.forgecity.ui.cityrender

import android.graphics.drawable.Drawable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import buzz.delena.forgecity.city.BuildingHitGeometry
import buzz.delena.forgecity.city.District
import buzz.delena.forgecity.city.DistrictSilhouette
import buzz.delena.forgecity.city.IsoLayout
import buzz.delena.forgecity.city.IsoMath
import buzz.delena.forgecity.city.IsoPoint
import buzz.delena.forgecity.city.RoofStyle
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/**
 * Slice B canvas helpers. All building drawing keeps the shared prism footprint
 * ([IsoLayout.HALF_W]/[IsoLayout.HALF_H]) and [BuildingHitGeometry.prismHeight],
 * so hit geometry stays compatible — only the top silhouette and surface polish vary.
 */

fun districtColor(district: District): Color = when (district) {
    District.FORGE -> Color(0xFFE8A15A)
    District.VAULT -> Color(0xFFD4AF37)
    District.NEXUS -> Color(0xFF4FD1C5)
    District.ARENA -> Color(0xFFE5738A)
    District.GARDEN -> Color(0xFF7CB342)
    District.ARCHIVE -> Color(0xFF90A4AE)
    District.CUSTOM -> Color(0xFFB39DDB)
}

private fun districtAccent(district: District): Color = when (district) {
    District.FORGE -> Color(0xFFFFC98A)
    District.VAULT -> Color(0xFFFFE49A)
    District.NEXUS -> Color(0xFF8CF5EA)
    District.ARENA -> Color(0xFFFFA7BC)
    District.GARDEN -> Color(0xFFBEE79A)
    District.ARCHIVE -> Color(0xFFD6E2EA)
    District.CUSTOM -> Color(0xFFE1D2FF)
}

/**
 * Subtle isometric ground plane so the city no longer floats in a void.
 * Draws a large soft diamond pad plus faint grid tiles and a cross road.
 */
fun DrawScope.drawGroundPlane(ambientEnabled: Boolean) {
    val w = IsoLayout.TILE_WIDTH
    val h = IsoLayout.TILE_HEIGHT
    val minC = -1f
    val maxC = 7f

    fun grid(c: Float, r: Float): Offset {
        val p = IsoMath.gridToScreen(c, r, w, h)
        return Offset(p.x, p.y + IsoLayout.HALF_H)
    }

    val pad = Path().apply {
        val a = grid(minC, minC)
        val b = grid(maxC, minC)
        val c = grid(maxC, maxC)
        val d = grid(minC, maxC)
        moveTo(a.x, a.y)
        lineTo(b.x, b.y)
        lineTo(c.x, c.y)
        lineTo(d.x, d.y)
        close()
    }
    drawPath(pad, Color(0x2612101C))
    drawPath(pad, Color(0x1AFFFFFF), style = Stroke(width = 1.2f))

    val gridAlpha = if (ambientEnabled) 0x1E else 0x12
    val line = Color(0xFF3A2E52).copy(alpha = gridAlpha / 255f)
    var i = minC
    while (i <= maxC) {
        val a = grid(i, minC)
        val b = grid(i, maxC)
        drawLine(line, Offset(a.x, a.y), Offset(b.x, b.y), strokeWidth = 1f)
        val c = grid(minC, i)
        val d = grid(maxC, i)
        drawLine(line, Offset(c.x, c.y), Offset(d.x, d.y), strokeWidth = 1f)
        i += 1f
    }

    // Cross roads through the district midlines.
    val road = Color(0x33101018)
    listOf(3f).forEach { m ->
        val a = grid(m, minC)
        val b = grid(m, maxC)
        drawLine(road, Offset(a.x, a.y), Offset(b.x, b.y), strokeWidth = 10f)
        val c = grid(minC, m)
        val d = grid(maxC, m)
        drawLine(road, Offset(c.x, c.y), Offset(d.x, d.y), strokeWidth = 10f)
    }
}

/**
 * Draw a full building: contact shadow, shaded facade, level windows,
 * night glow, district silhouette, favorite pin, and rounded icon badge (LOD-aware).
 */
fun DrawScope.drawCityBuilding(
    point: IsoPoint,
    district: District,
    level: Int,
    nightGlow: Boolean,
    heightScale: Float,
    pressed: Boolean,
    favorite: Boolean,
    scale: Float,
    icon: Drawable?,
) {
    val color = districtColor(district)
    val accent = districtAccent(district)
    val height = BuildingHitGeometry.prismHeight(level) * heightScale
    val halfW = IsoLayout.HALF_W * if (pressed) 1.06f else 1f
    val halfH = IsoLayout.HALF_H * if (pressed) 1.06f else 1f
    val px = point.x
    val py = point.y

    // Soft contact shadow under the base.
    val shadow = Path().apply {
        val cy = py + halfH + 4f
        moveTo(px, cy - halfH * 0.7f)
        lineTo(px + halfW * 1.18f, cy)
        lineTo(px, cy + halfH * 0.7f)
        lineTo(px - halfW * 1.18f, cy)
        close()
    }
    drawPath(shadow, Color(0x33000000))

    // Base footprint.
    val base = Path().apply {
        moveTo(px, py)
        lineTo(px + halfW, py + halfH)
        lineTo(px, py + halfH * 2)
        lineTo(px - halfW, py + halfH)
        close()
    }
    drawPath(base, color.copy(alpha = 0.5f))

    // Left wall (shaded darker).
    val leftWall = Path().apply {
        moveTo(px - halfW, py + halfH)
        lineTo(px, py + halfH * 2)
        lineTo(px, py + halfH * 2 - height)
        lineTo(px - halfW, py + halfH - height)
        close()
    }
    drawPath(leftWall, color.copy(alpha = 0.68f))
    // vertical shade gradient hint
    drawPath(leftWall, Color(0x22000000))

    // Right wall (lit brighter).
    val rightWall = Path().apply {
        moveTo(px + halfW, py + halfH)
        lineTo(px, py + halfH * 2)
        lineTo(px, py + halfH * 2 - height)
        lineTo(px + halfW, py + halfH - height)
        close()
    }
    drawPath(rightWall, color.copy(alpha = 0.94f))
    drawPath(rightWall, Color(0x18FFFFFF))

    // Roof diamond.
    val roof = Path().apply {
        moveTo(px, py - height)
        lineTo(px + halfW, py + halfH - height)
        lineTo(px, py + halfH * 2 - height)
        lineTo(px - halfW, py + halfH - height)
        close()
    }
    drawPath(roof, color)
    drawPath(roof, Color.White.copy(alpha = 0.16f), style = Stroke(width = 1.2f))

    // Windows — count scales with level, emissive at night.
    drawWindows(px, py, halfW, halfH, height, level, nightGlow, accent)

    // Cleaner night glow: a small soft halo instead of a large blob.
    if (nightGlow) {
        val gy = py + halfH - height
        drawCircle(
            color = accent.copy(alpha = 0.22f),
            radius = halfW * 0.85f,
            center = Offset(px, gy),
        )
    }

    // District silhouette on the roof.
    drawRoofSilhouette(px, py, halfW, halfH, height, DistrictSilhouette.of(district), color, accent, nightGlow)

    // Favorite pin: gold ring + pennant, not a flat orange fill.
    if (favorite) {
        drawFavoritePin(px, py, halfW, halfH, height)
    }

    if (pressed) {
        drawCircle(
            color = Color(0x66FFFFFF),
            radius = halfW * 0.7f,
            center = Offset(px, py + halfH - height),
        )
    }

    // Icon badge with LOD.
    drawIconBadge(px, py, halfH, height, scale, icon, color, accent)
}

private fun DrawScope.drawWindows(
    px: Float,
    py: Float,
    halfW: Float,
    halfH: Float,
    height: Float,
    level: Int,
    night: Boolean,
    accent: Color,
) {
    if (height < 14f) return
    val rows = (1 + level).coerceIn(2, 6)
    val lit = if (night) accent.copy(alpha = 0.9f) else Color(0x33FFFFFF)

    // Right wall corners.
    val rBotOuter = Offset(px + halfW, py + halfH)
    val rBotFront = Offset(px, py + halfH * 2)
    val rTopOuter = Offset(px + halfW, py + halfH - height)
    val rTopFront = Offset(px, py + halfH * 2 - height)
    // Left wall corners.
    val lBotOuter = Offset(px - halfW, py + halfH)
    val lTopOuter = Offset(px - halfW, py + halfH - height)

    fun lerp(a: Offset, b: Offset, t: Float) = Offset(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t)

    for (r in 1..rows) {
        val v = r / (rows + 1f)
        // Right wall: two columns.
        listOf(0.32f, 0.68f).forEach { u ->
            val bottom = lerp(rBotOuter, rBotFront, u)
            val top = lerp(rTopOuter, rTopFront, u)
            val p = lerp(bottom, top, v)
            drawCircle(lit, radius = 1.7f, center = p)
        }
        // Left wall: one column (dimmer side).
        val lb = lerp(lBotOuter, rBotFront, 0.5f)
        val lt = lerp(lTopOuter, rTopFront, 0.5f)
        val lp = lerp(lb, lt, v)
        drawCircle(lit.copy(alpha = lit.alpha * 0.6f), radius = 1.5f, center = lp)
    }
}

private fun DrawScope.drawRoofSilhouette(
    px: Float,
    py: Float,
    halfW: Float,
    halfH: Float,
    height: Float,
    style: RoofStyle,
    color: Color,
    accent: Color,
    night: Boolean,
) {
    val roofCy = py + halfH - height
    val stroke = Stroke(width = 2f, cap = StrokeCap.Round)
    when (style) {
        RoofStyle.SPIRE -> {
            val apex = Offset(px, roofCy - height * 0.55f - 14f)
            val p = Path().apply {
                moveTo(px - halfW * 0.28f, roofCy)
                lineTo(apex.x, apex.y)
                lineTo(px + halfW * 0.28f, roofCy)
                close()
            }
            drawPath(p, color)
            drawPath(p, accent.copy(alpha = 0.7f), style = stroke)
            drawCircle(accent, radius = 2.6f, center = apex)
        }
        RoofStyle.GOLD_CAP -> {
            val cap = Path().apply {
                val t = 8f
                moveTo(px, roofCy - t - halfH)
                lineTo(px + halfW * 0.7f, roofCy - t)
                lineTo(px, roofCy - t + halfH)
                lineTo(px - halfW * 0.7f, roofCy - t)
                close()
            }
            drawPath(cap, Color(0xFFF2CF5E))
            drawPath(cap, Color(0xFFFFF0B8).copy(alpha = 0.8f), style = stroke)
        }
        RoofStyle.ANTENNA -> {
            val topY = roofCy - height * 0.5f - 18f
            drawLine(accent, Offset(px, roofCy), Offset(px, topY), strokeWidth = 2f, cap = StrokeCap.Round)
            drawLine(
                accent.copy(alpha = 0.7f),
                Offset(px - 7f, topY + 6f),
                Offset(px + 7f, topY + 6f),
                strokeWidth = 1.6f,
            )
            drawCircle(if (night) accent else accent.copy(alpha = 0.85f), radius = 2.8f, center = Offset(px, topY))
        }
        RoofStyle.ANGULAR -> {
            val p = Path().apply {
                moveTo(px - halfW * 0.6f, roofCy)
                lineTo(px - halfW * 0.2f, roofCy - halfH - 6f)
                lineTo(px + halfW * 0.05f, roofCy - 2f)
                lineTo(px + halfW * 0.35f, roofCy - halfH - 12f)
                lineTo(px + halfW * 0.6f, roofCy)
                close()
            }
            drawPath(p, color)
            drawPath(p, accent.copy(alpha = 0.6f), style = stroke)
        }
        RoofStyle.SOFT -> {
            val p = Path().apply {
                val rw = halfW * 0.55f
                moveTo(px - rw, roofCy)
                cubicTo(
                    px - rw, roofCy - halfH - 10f,
                    px + rw, roofCy - halfH - 10f,
                    px + rw, roofCy,
                )
                close()
            }
            drawPath(p, color)
            drawPath(p, accent.copy(alpha = 0.55f), style = stroke)
        }
        RoofStyle.STEPPED -> {
            var w = halfW * 0.75f
            var y = roofCy
            repeat(3) {
                val step = Path().apply {
                    moveTo(px, y - halfH * 0.5f)
                    lineTo(px + w, y)
                    lineTo(px, y + halfH * 0.5f)
                    lineTo(px - w, y)
                    close()
                }
                drawPath(step, color.copy(alpha = 0.9f))
                drawPath(step, Color(0x22FFFFFF), style = Stroke(width = 1f))
                w *= 0.6f
                y -= 7f
            }
        }
        RoofStyle.DEFAULT -> {
            drawCircle(accent.copy(alpha = 0.5f), radius = 3f, center = Offset(px, roofCy - 4f))
        }
    }
}

private fun DrawScope.drawFavoritePin(
    px: Float,
    py: Float,
    halfW: Float,
    halfH: Float,
    height: Float,
) {
    val gold = Color(0xFFFFD764)
    // Gold ring around the base footprint.
    val ring = Path().apply {
        moveTo(px, py - 2f)
        lineTo(px + halfW + 3f, py + halfH)
        lineTo(px, py + halfH * 2 + 2f)
        lineTo(px - halfW - 3f, py + halfH)
        close()
    }
    drawPath(ring, gold.copy(alpha = 0.85f), style = Stroke(width = 2.4f))
    drawPath(ring, gold.copy(alpha = 0.12f))

    // Pennant flag above the roof.
    val roofCy = py + halfH - height
    val poleTop = Offset(px, roofCy - height * 0.4f - 22f)
    val poleBottom = Offset(px, roofCy - height * 0.4f)
    drawLine(gold, poleBottom, poleTop, strokeWidth = 1.8f, cap = StrokeCap.Round)
    val flag = Path().apply {
        moveTo(poleTop.x, poleTop.y)
        lineTo(poleTop.x + 12f, poleTop.y + 4f)
        lineTo(poleTop.x, poleTop.y + 8f)
        close()
    }
    drawPath(flag, gold)
}

private fun DrawScope.drawIconBadge(
    px: Float,
    py: Float,
    halfH: Float,
    height: Float,
    scale: Float,
    icon: Drawable?,
    color: Color,
    accent: Color,
) {
    val cx = px
    val cy = py + halfH - height - 6f
    val zoomedOut = scale < 0.9f
    val radius = if (zoomedOut) 9f else 15f

    // Subtle drop shadow.
    drawCircle(Color(0x40000000), radius = radius + 1.5f, center = Offset(cx, cy + 2f))
    // Plate.
    drawCircle(Color(0xFF1B1526), radius = radius, center = Offset(cx, cy))
    drawCircle(accent.copy(alpha = 0.85f), radius = radius, center = Offset(cx, cy), style = Stroke(width = 1.6f))

    if (zoomedOut || icon == null) {
        // LOD / no icon: just a color plate keyed to the district.
        drawCircle(color, radius = radius * 0.55f, center = Offset(cx, cy))
        return
    }

    val canvas = drawContext.canvas.nativeCanvas
    val save = canvas.save()
    val clip = android.graphics.Path().apply {
        addCircle(cx, cy, radius - 1.5f, android.graphics.Path.Direction.CW)
    }
    canvas.clipPath(clip)
    val d = (radius - 1.5f) * 2f
    val left = (cx - d / 2f).toInt()
    val topPx = (cy - d / 2f).toInt()
    icon.setBounds(left, topPx, left + d.toInt(), topPx + d.toInt())
    icon.draw(canvas)
    canvas.restoreToCount(save)
}

/** Slice B6: level-up burst with an expanding ring plus gold sparks. */
fun DrawScope.drawLevelUpBurst(
    point: IsoPoint,
    level: Int,
    progress: Float,
) {
    val roofTop = Offset(point.x, point.y - BuildingHitGeometry.prismHeight(level))
    val fade = (1f - progress).coerceIn(0f, 1f)
    val ringRadius = 12f + progress * 70f
    drawCircle(
        color = Color(0xFFFFE08A).copy(alpha = fade * 0.5f),
        radius = ringRadius,
        center = roofTop,
        style = Stroke(width = max(1f, 4f * fade)),
    )
    val particleCount = 14
    val spread = 24f + progress * 52f
    repeat(particleCount) { i ->
        val angle = (2.0 * Math.PI * i / particleCount).toFloat()
        val center = Offset(
            roofTop.x + cos(angle) * spread,
            roofTop.y + sin(angle) * spread * 0.6f - progress * 20f,
        )
        drawCircle(
            color = Color(0xFFFFE08A).copy(alpha = fade),
            radius = 3.2f * fade + 1.3f,
            center = center,
        )
    }
    drawCircle(
        color = Color(0xFFFFF3C4).copy(alpha = fade * 0.45f),
        radius = spread * 0.6f,
        center = roofTop,
    )
}
