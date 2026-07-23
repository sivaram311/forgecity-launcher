package buzz.delena.forgecity.ui.house

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import buzz.delena.forgecity.house.character.CharacterRole
import buzz.delena.forgecity.house.character.IdleHouseCharacter
import kotlin.math.sin

private object CharacterPalette {
    val mayor = Color(0xFF3D5A80)
    val mayorAccent = Color(0xFFC9A227)
    val assistant = Color(0xFF5C7A5E)
    val npc = Color(0xFF6B5344)
    val skin = Color(0xFFD4A574)
    val outline = Color(0xFF2A2118)
}

fun DrawScope.drawIdleCharacter(
    cell: Rect,
    character: IdleHouseCharacter,
    idlePhase: Float,
    speaking: Boolean = false,
) {
    val bob = sin((idlePhase + character.phaseOffset) * Math.PI.toFloat() * 2f) * 2.8f
    val breath = 1f + 0.04f * sin((idlePhase + character.phaseOffset) * Math.PI.toFloat() * 4f)
    val speakBoost = if (speaking && character.role == CharacterRole.ASSISTANT) {
        1f + 0.08f * sin(idlePhase * Math.PI.toFloat() * 8f)
    } else {
        1f
    }
    val base = Offset(
        cell.left + cell.width * character.nx.coerceIn(0.18f, 0.82f),
        cell.top + cell.height * character.ny.coerceIn(0.28f, 0.82f) + bob,
    )
    val scale = minOf(cell.width, cell.height) * 0.11f * breath * speakBoost
    val fill = when (character.role) {
        CharacterRole.MAYOR -> CharacterPalette.mayor
        CharacterRole.ASSISTANT -> CharacterPalette.assistant
        CharacterRole.NPC -> CharacterPalette.npc
    }

    if (speaking && character.role == CharacterRole.ASSISTANT) {
        drawCircle(
            color = Color(0x554FD1C5),
            radius = scale * 1.8f,
            center = base,
        )
    }

    // Soft contact shadow
    drawOval(
        color = Color(0x33000000),
        topLeft = Offset(base.x - scale * 0.55f, base.y + scale * 1.55f),
        size = androidx.compose.ui.geometry.Size(scale * 1.1f, scale * 0.28f),
    )

    // Legs
    drawLine(
        color = fill.copy(alpha = 0.9f),
        start = Offset(base.x - scale * 0.22f, base.y + scale * 0.55f),
        end = Offset(base.x - scale * 0.28f, base.y + scale * 1.55f),
        strokeWidth = scale * 0.22f,
    )
    drawLine(
        color = fill.copy(alpha = 0.9f),
        start = Offset(base.x + scale * 0.22f, base.y + scale * 0.55f),
        end = Offset(base.x + scale * 0.28f, base.y + scale * 1.55f),
        strokeWidth = scale * 0.22f,
    )

    // Torso (teardrop silhouette)
    val torso = Path().apply {
        moveTo(base.x, base.y - scale * 0.55f)
        cubicTo(
            base.x + scale * 0.55f,
            base.y - scale * 0.2f,
            base.x + scale * 0.48f,
            base.y + scale * 0.85f,
            base.x,
            base.y + scale * 0.95f,
        )
        cubicTo(
            base.x - scale * 0.48f,
            base.y + scale * 0.85f,
            base.x - scale * 0.55f,
            base.y - scale * 0.2f,
            base.x,
            base.y - scale * 0.55f,
        )
        close()
    }
    drawPath(torso, color = fill)

    // Arms
    drawLine(
        color = fill.copy(alpha = 0.85f),
        start = Offset(base.x - scale * 0.35f, base.y),
        end = Offset(base.x - scale * 0.7f, base.y + scale * 0.55f),
        strokeWidth = scale * 0.18f,
    )
    drawLine(
        color = fill.copy(alpha = 0.85f),
        start = Offset(base.x + scale * 0.35f, base.y),
        end = Offset(base.x + scale * 0.7f, base.y + scale * 0.55f),
        strokeWidth = scale * 0.18f,
    )

    // Head
    val headCenter = Offset(base.x, base.y - scale * 0.85f)
    drawCircle(color = CharacterPalette.skin, radius = scale * 0.38f, center = headCenter)
    drawCircle(
        color = CharacterPalette.outline.copy(alpha = 0.35f),
        radius = scale * 0.38f,
        center = headCenter,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2f),
    )

    // Mayor gold pin / assistant scarf hint
    when (character.role) {
        CharacterRole.MAYOR -> {
            drawCircle(
                color = CharacterPalette.mayorAccent,
                radius = scale * 0.1f,
                center = Offset(base.x, base.y + scale * 0.15f),
            )
        }
        CharacterRole.ASSISTANT -> {
            drawLine(
                color = Color(0xFFE8D9C4),
                start = Offset(base.x - scale * 0.2f, base.y - scale * 0.35f),
                end = Offset(base.x + scale * 0.2f, base.y - scale * 0.15f),
                strokeWidth = scale * 0.12f,
            )
        }
        CharacterRole.NPC -> Unit
    }
}
