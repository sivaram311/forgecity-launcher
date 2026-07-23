package buzz.delena.forgecity.ui.house

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.sin

/**
 * Wave 1 home surface: procedural Compose floor-plan house with warm interior lighting.
 * Apps appear as optional label markers at room hotspots (demo placement — not AppPlacementEngine).
 */
data class HouseLabelMarker(
    val id: String,
    val label: String,
    /** Room key matching [HouseRoom.id]. */
    val roomId: String,
    /** Normalized position inside the room (0–1). */
    val nx: Float = 0.5f,
    val ny: Float = 0.5f,
)

data class HouseRoom(
    val id: String,
    val title: String,
    /** Grid cell: col 0–1, row 0–2 in the 2×3 floor plan. */
    val col: Int,
    val row: Int,
    val floor: Color,
    val rug: Color,
)

private val HousePalette = object {
    val shell = Color(0xFF2A2118)
    val wall = Color(0xFFE8D9C4)
    val wallShadow = Color(0xFFC4B09A)
    val trim = Color(0xFF6B4E32)
    val woodDark = Color(0xFF5C4030)
    val lamp = Color(0xFFFFC978)
    val lampGlow = Color(0x66FFB347)
    val windowNight = Color(0xFF3A4A5C)
    val windowDay = Color(0xFFB8D4E8)
    val labelBg = Color(0xCC3D2E22)
    val labelText = Color(0xFFFFF4E6)
    val hallway = Color(0xFFD4C4A8)
}

val DefaultHouseRooms: List<HouseRoom> = listOf(
    HouseRoom("kitchen", "Kitchen", 0, 0, Color(0xFFC9A66B), Color(0xFF8F6A3E)),
    HouseRoom("living", "Living", 1, 0, Color(0xFFB8956A), Color(0xFF7A5138)),
    HouseRoom("hallway", "Hall", 0, 1, HousePalette.hallway, Color(0xFF9A8060)),
    HouseRoom("office", "Office", 1, 1, Color(0xFFA8885C), Color(0xFF6E4C32)),
    HouseRoom("bedroom", "Bedroom", 0, 2, Color(0xFFC4A882), Color(0xFF8B6848)),
    HouseRoom("workshop", "Workshop", 1, 2, Color(0xFFB07A4A), Color(0xFF6B4226)),
)

/** Demo district → room mapping for Wave 1 markers (A1 owns real placement). */
fun demoRoomIdForDistrict(districtName: String): String = when (districtName.uppercase()) {
    "GARDEN", "ARCHIVE" -> "kitchen"
    "NEXUS" -> "living"
    "CUSTOM" -> "hallway"
    "FORGE" -> "office"
    "VAULT" -> "bedroom"
    "ARENA" -> "workshop"
    else -> "living"
}

@Composable
fun HouseHomeSurface(
    markers: List<HouseLabelMarker> = emptyList(),
    rooms: List<HouseRoom> = DefaultHouseRooms,
    ambientEnabled: Boolean = true,
    night: Boolean = false,
    onMarkerTap: ((HouseLabelMarker) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var lampPhase by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current

    LaunchedEffect(ambientEnabled) {
        if (!ambientEnabled) return@LaunchedEffect
        while (true) {
            withFrameMillis { frame ->
                lampPhase = (frame % 3200L) / 3200f
            }
        }
    }

    // Slight foreshortening so the plan reads as a 3D-ish extruded interior.
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                rotationX = 48f
                cameraDistance = 14f * density.density
                // Keep pivots near center so chrome overlays still align visually.
                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.42f)
            }
            .pointerInput(markers, rooms) {
                if (onMarkerTap == null) return@pointerInput
                detectTapGestures { tap ->
                    val layout = houseLayout(size.width.toFloat(), size.height.toFloat(), rooms)
                    hitMarker(tap, layout, markers)?.let(onMarkerTap)
                }
            },
    ) {
        val layout = houseLayout(size.width, size.height, rooms)
        drawHouseBackdrop()
        drawHouseShell(layout)
        rooms.forEach { room ->
            val cell = layout.cellOf(room) ?: return@forEach
            drawRoom(cell, room, night, ambientEnabled, lampPhase)
        }
        drawInteriorWalls(layout)
        drawDoorways(layout)
        markers.forEach { marker ->
            val cell = layout.cellById(marker.roomId) ?: return@forEach
            drawMarker(cell, marker, density.density)
        }
    }
}

private data class HouseLayout(
    val origin: Offset,
    val houseW: Float,
    val houseH: Float,
    val cellW: Float,
    val cellH: Float,
    val rooms: List<HouseRoom>,
) {
    fun cellOf(room: HouseRoom): Rect? = cellAt(room.col, room.row)

    fun cellById(id: String): Rect? {
        val room = rooms.firstOrNull { it.id == id } ?: return null
        return cellOf(room)
    }

    fun cellAt(col: Int, row: Int): Rect? {
        if (col !in 0..1 || row !in 0..2) return null
        val left = origin.x + col * cellW
        val top = origin.y + row * cellH
        return Rect(left, top, left + cellW, top + cellH)
    }
}

private fun houseLayout(width: Float, height: Float, rooms: List<HouseRoom>): HouseLayout {
    val padX = width * 0.08f
    val padY = height * 0.10f
    val houseW = width - padX * 2f
    val houseH = height - padY * 2f
    return HouseLayout(
        origin = Offset(padX, padY),
        houseW = houseW,
        houseH = houseH,
        cellW = houseW / 2f,
        cellH = houseH / 3f,
        rooms = rooms,
    )
}

private fun hitMarker(
    tap: Offset,
    layout: HouseLayout,
    markers: List<HouseLabelMarker>,
): HouseLabelMarker? {
    // Reverse order so later (drawn-on-top) markers win.
    for (marker in markers.asReversed()) {
        val cell = layout.cellById(marker.roomId) ?: continue
        val center = Offset(
            cell.left + cell.width * marker.nx.coerceIn(0.12f, 0.88f),
            cell.top + cell.height * marker.ny.coerceIn(0.18f, 0.82f),
        )
        val radius = minOf(cell.width, cell.height) * 0.14f
        val dx = tap.x - center.x
        val dy = tap.y - center.y
        if (dx * dx + dy * dy <= radius * radius * 2.2f) return marker
    }
    return null
}

private fun DrawScope.drawHouseBackdrop() {
    drawRect(
        brush = Brush.verticalGradient(
            listOf(Color(0xFF1A1410), Color(0xFF2C2118), Color(0xFF1E1612)),
        ),
    )
}

private fun DrawScope.drawHouseShell(layout: HouseLayout) {
    val r = CornerRadius(18f, 18f)
    val outer = RoundRect(
        rect = Rect(
            layout.origin.x - 10f,
            layout.origin.y - 10f,
            layout.origin.x + layout.houseW + 10f,
            layout.origin.y + layout.houseH + 10f,
        ),
        cornerRadius = r,
    )
    drawPath(
        Path().apply { addRoundRect(outer) },
        color = HousePalette.shell,
    )
    // Soft inner floor wash.
    drawRoundRect(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x33FFD9A0), Color(0x00000000)),
            center = Offset(
                layout.origin.x + layout.houseW * 0.55f,
                layout.origin.y + layout.houseH * 0.35f,
            ),
            radius = layout.houseW * 0.7f,
        ),
        topLeft = layout.origin,
        size = Size(layout.houseW, layout.houseH),
        cornerRadius = CornerRadius(12f, 12f),
    )
}

private fun DrawScope.drawRoom(
    cell: Rect,
    room: HouseRoom,
    night: Boolean,
    ambientEnabled: Boolean,
    lampPhase: Float,
) {
    val inset = 3f
    val floor = Rect(
        cell.left + inset,
        cell.top + inset,
        cell.right - inset,
        cell.bottom - inset,
    )
    drawRect(color = room.floor, topLeft = floor.topLeft, size = floor.size)

    // Wood plank suggestion.
    val plankCount = 5
    for (i in 1 until plankCount) {
        val y = floor.top + floor.height * (i / plankCount.toFloat())
        drawLine(
            color = HousePalette.woodDark.copy(alpha = 0.12f),
            start = Offset(floor.left + 6f, y),
            end = Offset(floor.right - 6f, y),
            strokeWidth = 1.2f,
        )
    }

    // Rug / furniture pad.
    val rug = Rect(
        floor.left + floor.width * 0.18f,
        floor.top + floor.height * 0.28f,
        floor.right - floor.width * 0.18f,
        floor.bottom - floor.height * 0.22f,
    )
    drawRoundRect(
        color = room.rug.copy(alpha = 0.85f),
        topLeft = rug.topLeft,
        size = rug.size,
        cornerRadius = CornerRadius(8f, 8f),
    )

    // Extruded wall band (north edge) for 3D-ish depth.
    val wallH = floor.height * 0.14f
    drawRect(
        brush = Brush.verticalGradient(
            listOf(HousePalette.wall, HousePalette.wallShadow),
        ),
        topLeft = Offset(floor.left, floor.top),
        size = Size(floor.width, wallH),
    )

    // Window on north wall for outer rooms.
    if (room.row == 0 || room.col == 1 && room.row != 1) {
        val winW = floor.width * 0.28f
        val winH = wallH * 0.55f
        val winLeft = floor.left + floor.width * 0.36f
        val winTop = floor.top + wallH * 0.22f
        drawRoundRect(
            color = if (night) HousePalette.windowNight else HousePalette.windowDay,
            topLeft = Offset(winLeft, winTop),
            size = Size(winW, winH),
            cornerRadius = CornerRadius(3f, 3f),
        )
        drawRoundRect(
            color = HousePalette.trim.copy(alpha = 0.7f),
            topLeft = Offset(winLeft, winTop),
            size = Size(winW, winH),
            cornerRadius = CornerRadius(3f, 3f),
            style = Stroke(width = 1.5f),
        )
    }

    // Warm lamp pool.
    if (ambientEnabled) {
        val pulse = 0.85f + 0.15f * sin(lampPhase * Math.PI.toFloat() * 2f)
        val lampCenter = Offset(
            floor.left + floor.width * 0.72f,
            floor.top + floor.height * 0.38f,
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    HousePalette.lampGlow.copy(alpha = 0.55f * pulse),
                    Color.Transparent,
                ),
                center = lampCenter,
                radius = minOf(floor.width, floor.height) * 0.42f,
            ),
            radius = minOf(floor.width, floor.height) * 0.42f,
            center = lampCenter,
        )
        drawCircle(
            color = HousePalette.lamp.copy(alpha = 0.9f * pulse),
            radius = 4.5f,
            center = lampCenter,
        )
    }

    // Room title (small, warm).
    drawRoomTitle(room.title, Offset(floor.left + 10f, floor.top + wallH + 4f))
}

private fun DrawScope.drawRoomTitle(title: String, at: Offset) {
    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.argb(0xB3, 0x3D, 0x2E, 0x22)
        textSize = 28f * density
        isAntiAlias = true
        typeface = android.graphics.Typeface.create(
            android.graphics.Typeface.SANS_SERIF,
            android.graphics.Typeface.BOLD,
        )
    }
    drawContext.canvas.nativeCanvas.drawText(title, at.x, at.y + paint.textSize, paint)
}

private fun DrawScope.drawInteriorWalls(layout: HouseLayout) {
    val stroke = 5f
    val color = HousePalette.trim.copy(alpha = 0.55f)
    // Vertical split.
    val midX = layout.origin.x + layout.cellW
    drawLine(
        color = color,
        start = Offset(midX, layout.origin.y + 4f),
        end = Offset(midX, layout.origin.y + layout.houseH - 4f),
        strokeWidth = stroke,
        cap = StrokeCap.Round,
    )
    // Horizontal splits.
    for (row in 1..2) {
        val y = layout.origin.y + row * layout.cellH
        drawLine(
            color = color,
            start = Offset(layout.origin.x + 4f, y),
            end = Offset(layout.origin.x + layout.houseW - 4f, y),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}

private fun DrawScope.drawDoorways(layout: HouseLayout) {
    val gap = layout.cellW * 0.18f
    val color = Color(0xFF2A2118)
    // Door gaps on vertical wall (between kitchen/living, hallway/office, bedroom/workshop).
    for (row in 0..2) {
        val cell = layout.cellAt(0, row) ?: continue
        val midY = cell.center.y
        drawLine(
            color = color,
            start = Offset(layout.origin.x + layout.cellW, midY - gap * 0.35f),
            end = Offset(layout.origin.x + layout.cellW, midY + gap * 0.35f),
            strokeWidth = 7f,
            cap = StrokeCap.Round,
        )
    }
    // Door gaps on horizontal walls (hall connections).
    val hall = layout.cellById("hallway") ?: return
    val office = layout.cellById("office") ?: return
    drawLine(
        color = color,
        start = Offset(hall.center.x - gap * 0.4f, hall.bottom),
        end = Offset(hall.center.x + gap * 0.4f, hall.bottom),
        strokeWidth = 7f,
        cap = StrokeCap.Round,
    )
    drawLine(
        color = color,
        start = Offset(office.center.x - gap * 0.4f, office.top),
        end = Offset(office.center.x + gap * 0.4f, office.top),
        strokeWidth = 7f,
        cap = StrokeCap.Round,
    )
}

private fun DrawScope.drawMarker(cell: Rect, marker: HouseLabelMarker, density: Float) {
    val center = Offset(
        cell.left + cell.width * marker.nx.coerceIn(0.12f, 0.88f),
        cell.top + cell.height * marker.ny.coerceIn(0.18f, 0.82f),
    )
    val r = minOf(cell.width, cell.height) * 0.07f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xAAFFC978), Color(0x00FFC978)),
            center = center,
            radius = r * 2.4f,
        ),
        radius = r * 2.4f,
        center = center,
    )
    drawCircle(color = Color(0xFFFFE0A8), radius = r, center = center)
    drawCircle(
        color = HousePalette.trim,
        radius = r,
        center = center,
        style = Stroke(width = 1.8f),
    )

    val label = marker.label.take(10)
    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.argb(0xFF, 0xFF, 0xF4, 0xE6)
        textSize = 22f * density
        isAntiAlias = true
        textAlign = android.graphics.Paint.Align.CENTER
    }
    val bgPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.argb(0xCC, 0x3D, 0x2E, 0x22)
        isAntiAlias = true
    }
    val tw = paint.measureText(label)
    val pad = 8f * density
    val top = center.y + r + 6f * density
    drawContext.canvas.nativeCanvas.drawRoundRect(
        center.x - tw / 2f - pad,
        top - paint.textSize,
        center.x + tw / 2f + pad,
        top + pad * 0.4f,
        10f * density,
        10f * density,
        bgPaint,
    )
    drawContext.canvas.nativeCanvas.drawText(label, center.x, top - 2f, paint)
}
