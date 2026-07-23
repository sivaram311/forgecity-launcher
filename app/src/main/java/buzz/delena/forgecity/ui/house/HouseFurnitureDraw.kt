package buzz.delena.forgecity.ui.house

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

private object FurniturePalette {
    val wood = Color(0xFF5A3D28)
    val woodLight = Color(0xFF8B6344)
    val metal = Color(0xFF6A6E74)
    val metalDark = Color(0xFF3E4248)
    val stove = Color(0xFF2E3036)
    val burner = Color(0xFF1A1B1E)
    val screen = Color(0xFF1E2A3A)
    val screenGlow = Color(0xFF4A7A9A)
    val linen = Color(0xFFD8C8B0)
    val gold = Color(0xFFC9A227)
    val goldDark = Color(0xFF8A7018)
    val safe = Color(0xFF3A3F48)
}

/**
 * Room furniture silhouettes — readable interiors without photoreal detail.
 */
fun DrawScope.drawRoomFurniture(cell: Rect, roomId: String, wallH: Float) {
    val floor = Rect(
        cell.left + 3f,
        cell.top + 3f,
        cell.right - 3f,
        cell.bottom - 3f,
    )
    when (roomId) {
        "kitchen" -> {
            drawStove(floor, wallH)
            drawShelf(floor, nx = 0.12f, ny = 0.22f, tall = true)
            drawCounter(floor)
        }
        "living" -> {
            drawTv(floor, wallH)
            drawSofa(floor)
            drawShelf(floor, nx = 0.78f, ny = 0.22f, tall = true)
        }
        "hallway" -> {
            drawShelf(floor, nx = 0.18f, ny = 0.35f, tall = true)
            drawShelf(floor, nx = 0.72f, ny = 0.55f, tall = false)
        }
        "office" -> {
            drawDesk(floor, nx = 0.55f, ny = 0.55f)
            drawShelf(floor, nx = 0.12f, ny = 0.25f, tall = true)
            drawShelf(floor, nx = 0.12f, ny = 0.62f, tall = false)
        }
        "bedroom" -> {
            drawBed(floor)
            drawShelf(floor, nx = 0.78f, ny = 0.22f, tall = true)
            drawDesk(floor, nx = 0.28f, ny = 0.68f, compact = true)
        }
        "workshop" -> {
            drawDesk(floor, nx = 0.55f, ny = 0.62f)
            drawShelf(floor, nx = 0.12f, ny = 0.25f, tall = true)
            drawShelf(floor, nx = 0.82f, ny = 0.45f, tall = true)
        }
        "vault" -> {
            drawSafe(floor)
            drawShelf(floor, nx = 0.18f, ny = 0.22f, tall = true)
            drawDesk(floor, nx = 0.55f, ny = 0.62f, compact = true, goldTrim = true)
        }
    }
}

private fun DrawScope.drawDesk(
    floor: Rect,
    nx: Float,
    ny: Float,
    compact: Boolean = false,
    goldTrim: Boolean = false,
) {
    val w = floor.width * if (compact) 0.28f else 0.36f
    val h = floor.height * if (compact) 0.16f else 0.18f
    val left = floor.left + floor.width * nx - w / 2f
    val top = floor.top + floor.height * ny - h / 2f
    drawRoundRect(
        color = FurniturePalette.wood,
        topLeft = Offset(left, top),
        size = Size(w, h),
        cornerRadius = CornerRadius(4f, 4f),
    )
    drawRoundRect(
        color = FurniturePalette.woodLight.copy(alpha = 0.55f),
        topLeft = Offset(left + 3f, top + 2f),
        size = Size(w - 6f, h * 0.28f),
        cornerRadius = CornerRadius(2f, 2f),
    )
    // Legs
    val legW = 3.5f
    drawRect(
        color = FurniturePalette.wood.copy(alpha = 0.85f),
        topLeft = Offset(left + 4f, top + h),
        size = Size(legW, h * 0.35f),
    )
    drawRect(
        color = FurniturePalette.wood.copy(alpha = 0.85f),
        topLeft = Offset(left + w - 4f - legW, top + h),
        size = Size(legW, h * 0.35f),
    )
    if (goldTrim) {
        drawRoundRect(
            color = FurniturePalette.gold,
            topLeft = Offset(left, top),
            size = Size(w, h),
            cornerRadius = CornerRadius(4f, 4f),
            style = Stroke(width = 1.6f),
        )
    }
}

private fun DrawScope.drawShelf(floor: Rect, nx: Float, ny: Float, tall: Boolean) {
    val w = floor.width * 0.14f
    val h = floor.height * if (tall) 0.38f else 0.22f
    val left = floor.left + floor.width * nx
    val top = floor.top + floor.height * ny
    drawRoundRect(
        color = FurniturePalette.wood.copy(alpha = 0.92f),
        topLeft = Offset(left, top),
        size = Size(w, h),
        cornerRadius = CornerRadius(3f, 3f),
    )
    val shelves = if (tall) 3 else 2
    for (i in 1 until shelves) {
        val y = top + h * (i / shelves.toFloat())
        drawLine(
            color = FurniturePalette.woodLight.copy(alpha = 0.7f),
            start = Offset(left + 3f, y),
            end = Offset(left + w - 3f, y),
            strokeWidth = 2f,
        )
    }
}

private fun DrawScope.drawTv(floor: Rect, wallH: Float) {
    val w = floor.width * 0.32f
    val h = wallH * 0.72f
    val left = floor.left + floor.width * 0.34f
    val top = floor.top + wallH * 0.18f
    drawRoundRect(
        color = FurniturePalette.metalDark,
        topLeft = Offset(left - 3f, top - 2f),
        size = Size(w + 6f, h + 4f),
        cornerRadius = CornerRadius(3f, 3f),
    )
    drawRoundRect(
        color = FurniturePalette.screen,
        topLeft = Offset(left, top),
        size = Size(w, h),
        cornerRadius = CornerRadius(2f, 2f),
    )
    drawRoundRect(
        color = FurniturePalette.screenGlow.copy(alpha = 0.35f),
        topLeft = Offset(left + w * 0.15f, top + h * 0.2f),
        size = Size(w * 0.55f, h * 0.35f),
        cornerRadius = CornerRadius(2f, 2f),
    )
}

private fun DrawScope.drawSofa(floor: Rect) {
    val w = floor.width * 0.42f
    val h = floor.height * 0.2f
    val left = floor.left + floor.width * 0.28f
    val top = floor.top + floor.height * 0.58f
    drawRoundRect(
        color = Color(0xFF6B4A38),
        topLeft = Offset(left, top),
        size = Size(w, h),
        cornerRadius = CornerRadius(8f, 8f),
    )
    drawRoundRect(
        color = Color(0xFF8A6250).copy(alpha = 0.7f),
        topLeft = Offset(left + 6f, top + 4f),
        size = Size(w - 12f, h * 0.4f),
        cornerRadius = CornerRadius(6f, 6f),
    )
}

private fun DrawScope.drawBed(floor: Rect) {
    val w = floor.width * 0.48f
    val h = floor.height * 0.36f
    val left = floor.left + floor.width * 0.26f
    val top = floor.top + floor.height * 0.38f
    drawRoundRect(
        color = FurniturePalette.wood,
        topLeft = Offset(left, top),
        size = Size(w, h),
        cornerRadius = CornerRadius(5f, 5f),
    )
    drawRoundRect(
        color = FurniturePalette.linen,
        topLeft = Offset(left + 4f, top + h * 0.18f),
        size = Size(w - 8f, h * 0.72f),
        cornerRadius = CornerRadius(4f, 4f),
    )
    // Pillow
    drawRoundRect(
        color = Color(0xFFEDE4D4),
        topLeft = Offset(left + 8f, top + 6f),
        size = Size(w * 0.28f, h * 0.22f),
        cornerRadius = CornerRadius(4f, 4f),
    )
}

private fun DrawScope.drawStove(floor: Rect, wallH: Float) {
    val w = floor.width * 0.28f
    val h = floor.height * 0.22f
    val left = floor.left + floor.width * 0.58f
    val top = floor.top + wallH + floor.height * 0.08f
    drawRoundRect(
        color = FurniturePalette.stove,
        topLeft = Offset(left, top),
        size = Size(w, h),
        cornerRadius = CornerRadius(4f, 4f),
    )
    val burnerR = minOf(w, h) * 0.14f
    val cx = left + w * 0.32f
    val cy = top + h * 0.42f
    drawCircle(color = FurniturePalette.burner, radius = burnerR, center = Offset(cx, cy))
    drawCircle(
        color = FurniturePalette.burner,
        radius = burnerR,
        center = Offset(left + w * 0.68f, cy),
    )
    drawCircle(
        color = FurniturePalette.metal,
        radius = burnerR,
        center = Offset(cx, cy),
        style = Stroke(width = 1.5f),
    )
    drawCircle(
        color = FurniturePalette.metal,
        radius = burnerR,
        center = Offset(left + w * 0.68f, cy),
        style = Stroke(width = 1.5f),
    )
}

private fun DrawScope.drawCounter(floor: Rect) {
    val w = floor.width * 0.5f
    val h = floor.height * 0.12f
    val left = floor.left + floor.width * 0.12f
    val top = floor.top + floor.height * 0.72f
    drawRoundRect(
        color = FurniturePalette.woodLight,
        topLeft = Offset(left, top),
        size = Size(w, h),
        cornerRadius = CornerRadius(3f, 3f),
    )
}

private fun DrawScope.drawSafe(floor: Rect) {
    val w = floor.width * 0.42f
    val h = floor.height * 0.28f
    val left = floor.left + floor.width * 0.28f
    val top = floor.top + floor.height * 0.32f
    drawRoundRect(
        color = FurniturePalette.safe,
        topLeft = Offset(left, top),
        size = Size(w, h),
        cornerRadius = CornerRadius(6f, 6f),
    )
    drawRoundRect(
        color = FurniturePalette.goldDark,
        topLeft = Offset(left, top),
        size = Size(w, h),
        cornerRadius = CornerRadius(6f, 6f),
        style = Stroke(width = 2.2f),
    )
    drawCircle(
        color = FurniturePalette.gold,
        radius = minOf(w, h) * 0.12f,
        center = Offset(left + w * 0.72f, top + h * 0.5f),
    )
    drawCircle(
        color = FurniturePalette.metalDark,
        radius = minOf(w, h) * 0.05f,
        center = Offset(left + w * 0.72f, top + h * 0.5f),
    )
}
