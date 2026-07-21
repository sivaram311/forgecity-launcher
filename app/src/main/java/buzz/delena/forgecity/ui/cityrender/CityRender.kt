package buzz.delena.forgecity.ui.cityrender

import android.graphics.drawable.Drawable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/**
 * Slice B canvas helpers. All building drawing keeps the shared prism footprint
 * ([IsoLayout.HALF_W]/[IsoLayout.HALF_H]) and [BuildingHitGeometry.prismHeight],
 * so hit geometry stays compatible — only surface polish, shading, and rooftop
 * silhouettes vary.
 *
 * ## Lighting model
 * Drawing is driven by a lightweight, continuous lighting signal instead of a
 * single on/off flag:
 * - `nightFactor` (0f = full day, 1f = deep night) scales window emission, roof
 *   glow, rim light, and ground response. The legacy `nightGlow: Boolean` is kept
 *   for backward compatibility and simply maps to `nightFactor = 1f` when true.
 * - `timeSeconds` is a monotonic clock (seconds) used for *deterministic*
 *   flicker/pulse via `sin()`. Pass `0f` to freeze all animation (e.g. previews).
 *
 * Both new inputs are optional with safe defaults, so existing callers keep
 * working unchanged.
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

/** Multiply RGB by [factor] (dark < 1 < light) while keeping/overriding alpha. */
private fun Color.scaledRgb(factor: Float, alpha: Float = this.alpha): Color = Color(
    red = (red * factor).coerceIn(0f, 1f),
    green = (green * factor).coerceIn(0f, 1f),
    blue = (blue * factor).coerceIn(0f, 1f),
    alpha = alpha,
)

/** Blend two colors by [t] (0 = [this], 1 = [other]); used for night emission mixing. */
private fun Color.mix(other: Color, t: Float): Color {
    val k = t.coerceIn(0f, 1f)
    return Color(
        red = red + (other.red - red) * k,
        green = green + (other.green - green) * k,
        blue = blue + (other.blue - blue) * k,
        alpha = alpha + (other.alpha - alpha) * k,
    )
}

/** Deterministic 0f..1f hash from a cell's screen position — stable per building. */
private fun cellSeed(px: Float, py: Float): Float {
    val n = sin(px * 12.9898f + py * 78.233f) * 43758.547f
    return abs(n - kotlin.math.floor(n))
}

/**
 * Subtle isometric ground plane so the city no longer floats in a void.
 * Draws a large soft diamond pad plus faint grid tiles and a cross road.
 * [nightFactor] deepens the pad and cools the grid response after dusk.
 */
fun DrawScope.drawGroundPlane(ambientEnabled: Boolean, nightFactor: Float = 0f) {
    val w = IsoLayout.TILE_WIDTH
    val h = IsoLayout.TILE_HEIGHT
    val minC = -1f
    val maxC = 7f
    val nf = nightFactor.coerceIn(0f, 1f)

    fun grid(c: Float, r: Float): Offset {
        val p = IsoMath.gridToScreen(c, r, w, h)
        return Offset(p.x, p.y + IsoLayout.HALF_H)
    }

    val a = grid(minC, minC)
    val b = grid(maxC, minC)
    val c = grid(maxC, maxC)
    val d = grid(minC, maxC)
    val pad = Path().apply {
        moveTo(a.x, a.y)
        lineTo(b.x, b.y)
        lineTo(c.x, c.y)
        lineTo(d.x, d.y)
        close()
    }
    // Depth-shaded pad: darker toward the far edge, deeper at night.
    val near = Color(0x2612101C).mix(Color(0xCC08060F), 0.35f * nf)
    val far = Color(0x3A0A0812).mix(Color(0xDD05040A), 0.5f * nf)
    drawPath(
        pad,
        brush = Brush.verticalGradient(
            colors = listOf(far, near),
            startY = a.y,
            endY = c.y,
        ),
    )
    drawPath(pad, Color(0x1AFFFFFF), style = Stroke(width = 1.2f))

    val gridAlpha = if (ambientEnabled) 0x1E else 0x12
    // Grid lines pick up a faint cool glow at night for a "powered" feel.
    val lineBase = Color(0xFF3A2E52).copy(alpha = gridAlpha / 255f)
    val line = lineBase.mix(Color(0xFF6A54B0).copy(alpha = gridAlpha / 255f), nf)
    var i = minC
    while (i <= maxC) {
        val la = grid(i, minC)
        val lb = grid(i, maxC)
        drawLine(line, Offset(la.x, la.y), Offset(lb.x, lb.y), strokeWidth = 1f)
        val lc = grid(minC, i)
        val ld = grid(maxC, i)
        drawLine(line, Offset(lc.x, lc.y), Offset(ld.x, ld.y), strokeWidth = 1f)
        i += 1f
    }

    // Cross roads through the district midlines.
    val road = Color(0x33101018)
    listOf(3f).forEach { m ->
        val ra = grid(m, minC)
        val rb = grid(m, maxC)
        drawLine(road, Offset(ra.x, ra.y), Offset(rb.x, rb.y), strokeWidth = 10f)
        val rc = grid(minC, m)
        val rd = grid(maxC, m)
        drawLine(road, Offset(rc.x, rc.y), Offset(rd.x, rd.y), strokeWidth = 10f)
    }
}

/**
 * Draw a full building: contact shadow, shaded facade, level windows,
 * night glow, district silhouette, favorite pin, and rounded icon badge (LOD-aware).
 *
 * @param nightGlow legacy on/off night flag; still honored exactly as before.
 * @param nightFactor continuous 0f..1f lighting intensity (defaults from [nightGlow]).
 * @param timeSeconds monotonic clock for deterministic window flicker (0f = static).
 * @param activityPulse optional 0f..1f notification/activity highlight (0f = none).
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
    nightFactor: Float = if (nightGlow) 1f else 0f,
    timeSeconds: Float = 0f,
    activityPulse: Float = 0f,
) {
    val color = districtColor(district)
    val accent = districtAccent(district)
    val nf = nightFactor.coerceIn(0f, 1f)
    val height = BuildingHitGeometry.prismHeight(level) * heightScale
    val halfW = IsoLayout.HALF_W * if (pressed) 1.06f else 1f
    val halfH = IsoLayout.HALF_H * if (pressed) 1.06f else 1f
    val px = point.x
    val py = point.y

    // Y extents of the extruded walls (used for vertical shading gradients).
    val wallTopY = py + halfH - height
    val wallBotY = py + halfH * 2f

    // Soft contact shadow under the base — larger + softer for grounded depth.
    val shadow = Path().apply {
        val cy = py + halfH + 5f
        moveTo(px, cy - halfH * 0.8f)
        lineTo(px + halfW * 1.28f, cy)
        lineTo(px, cy + halfH * 0.8f)
        lineTo(px - halfW * 1.28f, cy)
        close()
    }
    drawPath(shadow, Color(0x22000000))
    // Tighter inner core shadow for contact definition.
    val coreShadow = Path().apply {
        val cy = py + halfH + 2f
        moveTo(px, cy - halfH * 0.55f)
        lineTo(px + halfW * 0.95f, cy)
        lineTo(px, cy + halfH * 0.55f)
        lineTo(px - halfW * 0.95f, cy)
        close()
    }
    drawPath(coreShadow, Color(0x33000000))

    // Base footprint (unchanged geometry).
    val base = Path().apply {
        moveTo(px, py)
        lineTo(px + halfW, py + halfH)
        lineTo(px, py + halfH * 2)
        lineTo(px - halfW, py + halfH)
        close()
    }
    drawPath(base, color.copy(alpha = 0.5f))

    // Left wall (shadowed) — gradient from a lit top to a deeper base + inner shadow.
    val leftWall = Path().apply {
        moveTo(px - halfW, py + halfH)
        lineTo(px, py + halfH * 2)
        lineTo(px, py + halfH * 2 - height)
        lineTo(px - halfW, py + halfH - height)
        close()
    }
    drawPath(
        leftWall,
        brush = Brush.verticalGradient(
            colors = listOf(
                color.scaledRgb(0.78f, alpha = 0.7f),
                color.scaledRgb(0.55f, alpha = 0.72f),
            ),
            startY = wallTopY,
            endY = wallBotY,
        ),
    )
    // Inner shadow hugging the front vertical seam for a crisp corner.
    val leftSeamShadow = Path().apply {
        moveTo(px, py + halfH * 2)
        lineTo(px, py + halfH * 2 - height)
        lineTo(px - halfW * 0.36f, py + halfH - height + halfH * 0.36f)
        lineTo(px - halfW * 0.36f, py + halfH * 2 - height * 0.02f)
        close()
    }
    drawPath(leftSeamShadow, Color(0x1E000000))

    // Right wall (lit) — gradient from a bright top edge down to the base color.
    val rightWall = Path().apply {
        moveTo(px + halfW, py + halfH)
        lineTo(px, py + halfH * 2)
        lineTo(px, py + halfH * 2 - height)
        lineTo(px + halfW, py + halfH - height)
        close()
    }
    drawPath(
        rightWall,
        brush = Brush.verticalGradient(
            colors = listOf(
                color.scaledRgb(1.12f, alpha = 0.96f),
                color.scaledRgb(0.9f, alpha = 0.92f),
            ),
            startY = wallTopY,
            endY = wallBotY,
        ),
    )
    // Soft sheen highlight on the lit wall's upper band.
    drawPath(rightWall, Color(0x14FFFFFF))

    // Roof diamond.
    val roof = Path().apply {
        moveTo(px, py - height)
        lineTo(px + halfW, py + halfH - height)
        lineTo(px, py + halfH * 2 - height)
        lineTo(px - halfW, py + halfH - height)
        close()
    }
    drawPath(roof, color.scaledRgb(1.05f))
    // Rim light along the two top-facing roof edges (upper-left + upper-right).
    val rimAlpha = 0.22f + 0.28f * nf
    drawLine(
        Color.White.copy(alpha = rimAlpha),
        Offset(px - halfW, py + halfH - height),
        Offset(px, py - height),
        strokeWidth = 1.6f,
        cap = StrokeCap.Round,
    )
    drawLine(
        Color.White.copy(alpha = rimAlpha * 0.7f),
        Offset(px, py - height),
        Offset(px + halfW, py + halfH - height),
        strokeWidth = 1.6f,
        cap = StrokeCap.Round,
    )
    drawPath(roof, Color.White.copy(alpha = 0.12f), style = Stroke(width = 1.2f))

    // Windows — count scales with level, emissive + flicker at night.
    drawWindows(px, py, halfW, halfH, height, level, nf, timeSeconds, accent)

    // Night glow: a soft accent halo that intensifies with nightFactor.
    if (nf > 0.01f) {
        val gy = py + halfH - height
        drawCircle(
            color = accent.copy(alpha = 0.10f + 0.16f * nf),
            radius = halfW * (0.8f + 0.15f * nf),
            center = Offset(px, gy),
        )
    }

    // District silhouette on the roof.
    drawRoofSilhouette(px, py, halfW, halfH, height, DistrictSilhouette.of(district), color, accent, nf)

    // Optional activity/notification pulse above the roof (behind a parameter).
    if (activityPulse > 0.001f) {
        drawActivityPulse(Offset(px, py + halfH - height), halfW, activityPulse, accent)
    }

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
    nightFactor: Float,
    timeSeconds: Float,
    accent: Color,
) {
    if (height < 14f) return
    val nf = nightFactor.coerceIn(0f, 1f)
    val seed = cellSeed(px, py)
    val rows = (1 + level).coerceIn(2, 6)

    // Day: faint recessed glass. Night: warm emissive keyed to the district accent.
    val dayGlass = Color(0x2AFFFFFF)
    val nightLit = accent.mix(Color.White, 0.35f)

    // Right wall corners.
    val rBotOuter = Offset(px + halfW, py + halfH)
    val rBotFront = Offset(px, py + halfH * 2)
    val rTopOuter = Offset(px + halfW, py + halfH - height)
    val rTopFront = Offset(px, py + halfH * 2 - height)
    // Left wall corners.
    val lBotOuter = Offset(px - halfW, py + halfH)
    val lTopOuter = Offset(px - halfW, py + halfH - height)

    fun lerp(a: Offset, b: Offset, t: Float) = Offset(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t)

    // Deterministic per-window emission: some panes dark, others flicker gently.
    fun emission(key: Float): Float {
        val h = abs(sin((key + seed) * 91.7f) * 4137.13f).let { it - kotlin.math.floor(it) }
        if (h < 0.18f) return 0f // this pane is unlit
        val flicker = if (timeSeconds > 0f) 0.8f + 0.2f * sin(timeSeconds * 2.1f + key * 6.283f + seed * 10f) else 1f
        return (0.55f + 0.45f * h) * flicker
    }

    for (r in 1..rows) {
        val v = r / (rows + 1f)
        // Right (lit) wall: two columns.
        listOf(0.30f, 0.70f).forEach { u ->
            val bottom = lerp(rBotOuter, rBotFront, u)
            val top = lerp(rTopOuter, rTopFront, u)
            val p = lerp(bottom, top, v)
            val e = emission(r * 10f + u)
            if (nf > 0.01f && e > 0f) {
                // Emissive halo + bright core.
                drawCircle(nightLit.copy(alpha = 0.35f * nf * e), radius = 3.6f, center = p)
                drawCircle(nightLit.copy(alpha = (0.55f + 0.4f * nf) * e), radius = 1.8f, center = p)
            } else {
                drawCircle(dayGlass, radius = 1.7f, center = p)
            }
        }
        // Left (shadowed) wall: one dimmer column.
        val lb = lerp(lBotOuter, rBotFront, 0.5f)
        val lt = lerp(lTopOuter, rTopFront, 0.5f)
        val lp = lerp(lb, lt, v)
        val le = emission(r * 10f + 0.5f)
        if (nf > 0.01f && le > 0f) {
            drawCircle(nightLit.copy(alpha = 0.28f * nf * le), radius = 3f, center = lp)
            drawCircle(nightLit.copy(alpha = (0.4f + 0.3f * nf) * le), radius = 1.5f, center = lp)
        } else {
            drawCircle(dayGlass.copy(alpha = dayGlass.alpha * 0.6f), radius = 1.5f, center = lp)
        }
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
    nightFactor: Float,
) {
    val nf = nightFactor.coerceIn(0f, 1f)
    val night = nf > 0.01f
    val roofCy = py + halfH - height
    val stroke = Stroke(width = 2f, cap = StrokeCap.Round)
    // Emissive treatment reused across roof types at night.
    fun emissive(center: Offset, radius: Float) {
        if (!night) return
        drawCircle(accent.copy(alpha = 0.3f * nf), radius = radius * 1.9f, center = center)
        drawCircle(accent.mix(Color.White, 0.4f).copy(alpha = 0.6f * nf), radius = radius, center = center)
    }
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
            emissive(apex, 2.6f)
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
            if (night) {
                drawPath(cap, Color(0xFFFFE49A).copy(alpha = 0.25f * nf))
            }
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
            emissive(Offset(px, topY), 2.8f)
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
            if (night) {
                drawPath(p, accent.copy(alpha = 0.18f * nf))
            }
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
            if (night) {
                drawPath(p, accent.copy(alpha = 0.16f * nf))
            }
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
            if (night) {
                drawCircle(accent.copy(alpha = 0.4f * nf), radius = 3.2f, center = Offset(px, y + 3f))
            }
        }
        RoofStyle.DEFAULT -> {
            drawCircle(accent.copy(alpha = 0.5f), radius = 3f, center = Offset(px, roofCy - 4f))
            emissive(Offset(px, roofCy - 4f), 3f)
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

/**
 * Reusable expanding spark ring, shared by the level-up burst and activity pulse.
 * [center] anchors the effect, [progress] runs 0f..1f (fades out toward 1f).
 */
private fun DrawScope.drawSparkRing(
    center: Offset,
    progress: Float,
    baseRadius: Float,
    growth: Float,
    ringColor: Color,
    sparkColor: Color,
    particleCount: Int,
) {
    val fade = (1f - progress).coerceIn(0f, 1f)
    val ringRadius = baseRadius + progress * growth
    drawCircle(
        color = ringColor.copy(alpha = fade * 0.5f),
        radius = ringRadius,
        center = center,
        style = Stroke(width = max(1f, 4f * fade)),
    )
    val spread = baseRadius * 2f + progress * growth * 0.75f
    repeat(particleCount) { i ->
        val angle = (2.0 * Math.PI * i / particleCount).toFloat()
        val p = Offset(
            center.x + cos(angle) * spread,
            center.y + sin(angle) * spread * 0.6f - progress * 20f,
        )
        drawCircle(
            color = sparkColor.copy(alpha = fade),
            radius = 3.2f * fade + 1.3f,
            center = p,
        )
    }
    drawCircle(
        color = sparkColor.copy(alpha = fade * 0.4f),
        radius = spread * 0.6f,
        center = center,
    )
}

/**
 * Soft, looping notification/activity highlight above a building roof.
 * Kept subtle and behind the optional `activityPulse` parameter so idle cities
 * stay calm. [strength] 0f..1f controls brightness.
 */
private fun DrawScope.drawActivityPulse(
    roofTop: Offset,
    halfW: Float,
    strength: Float,
    accent: Color,
) {
    val s = strength.coerceIn(0f, 1f)
    drawCircle(
        color = accent.copy(alpha = 0.28f * s),
        radius = halfW * (0.7f + 0.5f * s),
        center = roofTop,
        style = Stroke(width = 2f),
    )
    drawCircle(
        color = accent.mix(Color.White, 0.5f).copy(alpha = 0.5f * s),
        radius = halfW * 0.18f,
        center = roofTop,
    )
}

/** Slice B6: level-up burst with an expanding ring plus gold sparks. */
fun DrawScope.drawLevelUpBurst(
    point: IsoPoint,
    level: Int,
    progress: Float,
) {
    val roofTop = Offset(point.x, point.y - BuildingHitGeometry.prismHeight(level))
    drawSparkRing(
        center = roofTop,
        progress = progress,
        baseRadius = 12f,
        growth = 70f,
        ringColor = Color(0xFFFFE08A),
        sparkColor = Color(0xFFFFE08A),
        particleCount = 14,
    )
}
