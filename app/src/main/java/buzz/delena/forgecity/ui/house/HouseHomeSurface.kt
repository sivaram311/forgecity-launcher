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
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import buzz.delena.forgecity.house.character.DefaultIdleHouseCharacters
import buzz.delena.forgecity.house.character.IdleHouseCharacter
import kotlin.math.sin

/**
 * Wave 2 home surface: procedural Compose floor-plan with Vault annex, furniture
 * silhouettes, and budget-capped idle characters (no Filament/glTF).
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
    /**
     * Grid cell: col 0–1, row 0–2 for the main 2×3 plan.
     * Vault uses [col]=2 as the east annex beside office (row 1).
     */
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
    val hallway = Color(0xFFD4C4A8)
    val vaultFloor = Color(0xFF8A7340)
    val vaultRug = Color(0xFFC9A227)
}

val DefaultHouseRooms: List<HouseRoom> = listOf(
    HouseRoom("kitchen", "Kitchen", 0, 0, Color(0xFFC9A66B), Color(0xFF8F6A3E)),
    HouseRoom("living", "Living", 1, 0, Color(0xFFB8956A), Color(0xFF7A5138)),
    HouseRoom("hallway", "Hall", 0, 1, HousePalette.hallway, Color(0xFF9A8060)),
    HouseRoom("office", "Office", 1, 1, Color(0xFFA8885C), Color(0xFF6E4C32)),
    HouseRoom("bedroom", "Bedroom", 0, 2, Color(0xFFC4A882), Color(0xFF8B6848)),
    HouseRoom("workshop", "Workshop", 1, 2, Color(0xFFB07A4A), Color(0xFF6B4226)),
    // East annex beside office — matches domain HouseRoom.VAULT footprint.
    HouseRoom("vault", "Vault", 2, 1, HousePalette.vaultFloor, HousePalette.vaultRug),
)

/** Demo district → room mapping for markers. */
fun demoRoomIdForDistrict(districtName: String): String = when (districtName.uppercase()) {
    "GARDEN", "ARCHIVE" -> "kitchen"
    "NEXUS" -> "living"
    "CUSTOM" -> "hallway"
    "FORGE" -> "office"
    "VAULT" -> "vault"
    "ARENA" -> "workshop"
    else -> "living"
}

@Composable
fun HouseHomeSurface(
    markers: List<HouseLabelMarker> = emptyList(),
    rooms: List<HouseRoom> = DefaultHouseRooms,
    characters: List<IdleHouseCharacter> = DefaultIdleHouseCharacters,
    ambientEnabled: Boolean = true,
    /** Soft lamp pools; typically [AnimationBudget.allowsSoftShadows]. */
    allowsSoftShadows: Boolean = true,
    /** Cap idle humanoids; typically [AnimationBudget.maxActiveCharacters]. */
    maxCharacters: Int = 3,
    /** When true, assistant character gets a speech pulse (from AssistantHouseBridge). */
    assistantSpeaking: Boolean = false,
    night: Boolean = false,
    onMarkerTap: ((HouseLabelMarker) -> Unit)? = null,
    onMarkerLongPress: ((HouseLabelMarker) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var lampPhase by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val activeCharacters = remember(characters, maxCharacters) {
        characters.take(maxCharacters.coerceAtLeast(0))
    }
    val animateAmbient = ambientEnabled && (allowsSoftShadows || activeCharacters.isNotEmpty())

    LaunchedEffect(animateAmbient) {
        if (!animateAmbient) return@LaunchedEffect
        while (true) {
            withFrameMillis { frame ->
                lampPhase = (frame % 3200L) / 3200f
            }
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                rotationX = 48f
                cameraDistance = 14f * density.density
                transformOrigin = TransformOrigin(0.5f, 0.42f)
            }
            .pointerInput(markers, rooms, onMarkerTap, onMarkerLongPress) {
                if (onMarkerTap == null && onMarkerLongPress == null) return@pointerInput
                detectTapGestures(
                    onLongPress = { press ->
                        if (onMarkerLongPress == null) return@detectTapGestures
                        val layout = houseLayout(size.width.toFloat(), size.height.toFloat(), rooms)
                        hitMarker(press, layout, markers)?.let(onMarkerLongPress)
                    },
                    onTap = { tap ->
                        if (onMarkerTap == null) return@detectTapGestures
                        val layout = houseLayout(size.width.toFloat(), size.height.toFloat(), rooms)
                        hitMarker(tap, layout, markers)?.let(onMarkerTap)
                    },
                )
            },
    ) {
        val layout = houseLayout(size.width, size.height, rooms)
        drawHouseBackdrop()
        drawHouseShell(layout)
        rooms.forEach { room ->
            val cell = layout.cellOf(room) ?: return@forEach
            drawRoom(
                cell = cell,
                room = room,
                night = night,
                ambientEnabled = ambientEnabled,
                allowsSoftShadows = allowsSoftShadows,
                lampPhase = lampPhase,
            )
        }
        drawInteriorWalls(layout)
        drawDoorways(layout)
        activeCharacters.forEach { character ->
            val cell = layout.cellById(character.roomId) ?: return@forEach
            drawIdleCharacter(
                cell = cell,
                character = character,
                idlePhase = lampPhase,
                speaking = assistantSpeaking,
            )
        }
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
    /** Width of the main 2-column plan (excludes vault annex). */
    val mainW: Float,
    val cellW: Float,
    val cellH: Float,
    val vaultW: Float,
    val rooms: List<HouseRoom>,
) {
    fun cellOf(room: HouseRoom): Rect? = when {
        room.col == 2 -> vaultCell(room.row)
        else -> cellAt(room.col, room.row)
    }

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

    fun vaultCell(row: Int): Rect? {
        if (row != 1) return null
        val left = origin.x + mainW
        val top = origin.y + cellH
        return Rect(left, top, left + vaultW, top + cellH)
    }
}

private fun houseLayout(width: Float, height: Float, rooms: List<HouseRoom>): HouseLayout {
    val padX = width * 0.06f
    val padY = height * 0.10f
    val houseW = width - padX * 2f
    val houseH = height - padY * 2f
    // Domain footprint ~7 main + 2 vault → annex ≈ 22% of total width.
    val vaultW = houseW * 0.22f
    val mainW = houseW - vaultW
    return HouseLayout(
        origin = Offset(padX, padY),
        houseW = houseW,
        houseH = houseH,
        mainW = mainW,
        cellW = mainW / 2f,
        cellH = houseH / 3f,
        vaultW = vaultW,
        rooms = rooms,
    )
}

private fun hitMarker(
    tap: Offset,
    layout: HouseLayout,
    markers: List<HouseLabelMarker>,
): HouseLabelMarker? {
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
    // Main 2×3 body.
    val mainOuter = RoundRect(
        rect = Rect(
            layout.origin.x - 10f,
            layout.origin.y - 10f,
            layout.origin.x + layout.mainW + 6f,
            layout.origin.y + layout.houseH + 10f,
        ),
        cornerRadius = r,
    )
    drawPath(Path().apply { addRoundRect(mainOuter) }, color = HousePalette.shell)

    // Vault annex shell (east of office).
    val vault = layout.vaultCell(1)
    if (vault != null) {
        val annex = RoundRect(
            rect = Rect(
                vault.left - 4f,
                vault.top - 8f,
                vault.right + 10f,
                vault.bottom + 8f,
            ),
            cornerRadius = CornerRadius(14f, 14f),
        )
        drawPath(Path().apply { addRoundRect(annex) }, color = HousePalette.shell)
        // Gold rim hint on annex.
        drawRoundRect(
            color = HousePalette.vaultRug.copy(alpha = 0.35f),
            topLeft = Offset(vault.left - 2f, vault.top - 6f),
            size = Size(vault.width + 8f, vault.height + 12f),
            cornerRadius = CornerRadius(12f, 12f),
            style = Stroke(width = 2f),
        )
    }

    drawRoundRect(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x33FFD9A0), Color(0x00000000)),
            center = Offset(
                layout.origin.x + layout.mainW * 0.55f,
                layout.origin.y + layout.houseH * 0.35f,
            ),
            radius = layout.mainW * 0.7f,
        ),
        topLeft = layout.origin,
        size = Size(layout.mainW, layout.houseH),
        cornerRadius = CornerRadius(12f, 12f),
    )
}

private fun DrawScope.drawRoom(
    cell: Rect,
    room: HouseRoom,
    night: Boolean,
    ambientEnabled: Boolean,
    allowsSoftShadows: Boolean,
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

    val wallH = floor.height * 0.14f
    drawRect(
        brush = Brush.verticalGradient(
            listOf(HousePalette.wall, HousePalette.wallShadow),
        ),
        topLeft = Offset(floor.left, floor.top),
        size = Size(floor.width, wallH),
    )

    val showWindow = when (room.id) {
        "hallway" -> false
        "vault" -> true
        else -> room.row == 0 || (room.col == 1 && room.row != 1)
    }
    if (showWindow) {
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

    drawRoomFurniture(cell, room.id, wallH)

    if (ambientEnabled) {
        val pulse = 0.85f + 0.15f * sin(lampPhase * Math.PI.toFloat() * 2f)
        val lampCenter = Offset(
            floor.left + floor.width * 0.72f,
            floor.top + floor.height * 0.38f,
        )
        if (allowsSoftShadows) {
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
        }
        drawCircle(
            color = HousePalette.lamp.copy(alpha = 0.9f * pulse),
            radius = 4.5f,
            center = lampCenter,
        )
    }

    drawRoomTitle(room.title, Offset(floor.left + 8f, floor.top + wallH + 3f))
}

private fun DrawScope.drawRoomTitle(title: String, at: Offset) {
    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.argb(0xB3, 0x3D, 0x2E, 0x22)
        textSize = 26f * density
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
    val midX = layout.origin.x + layout.cellW
    drawLine(
        color = color,
        start = Offset(midX, layout.origin.y + 4f),
        end = Offset(midX, layout.origin.y + layout.houseH - 4f),
        strokeWidth = stroke,
        cap = StrokeCap.Round,
    )
    for (row in 1..2) {
        val y = layout.origin.y + row * layout.cellH
        drawLine(
            color = color,
            start = Offset(layout.origin.x + 4f, y),
            end = Offset(layout.origin.x + layout.mainW - 4f, y),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
    // Vault / office shared wall.
    val vault = layout.vaultCell(1) ?: return
    drawLine(
        color = color,
        start = Offset(vault.left, vault.top + 4f),
        end = Offset(vault.left, vault.bottom - 4f),
        strokeWidth = stroke,
        cap = StrokeCap.Round,
    )
}

private fun DrawScope.drawDoorways(layout: HouseLayout) {
    val gap = layout.cellW * 0.18f
    val color = Color(0xFF2A2118)
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
    // Office ↔ Vault doorway.
    val vault = layout.vaultCell(1) ?: return
    val midY = vault.center.y
    drawLine(
        color = color,
        start = Offset(vault.left, midY - gap * 0.35f),
        end = Offset(vault.left, midY + gap * 0.35f),
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
