package buzz.delena.forgecity.house.character

/**
 * Stylized idle occupant for the procedural Compose house (Wave 2 — no glTF).
 * Positions are normalized inside the target [roomId] floor cell (0–1).
 */
enum class CharacterRole {
    MAYOR,
    ASSISTANT,
    NPC,
}

data class IdleHouseCharacter(
    val id: String,
    val role: CharacterRole,
    val roomId: String,
    val nx: Float,
    val ny: Float,
    /** Phase offset so bob/breath desyncs across characters. */
    val phaseOffset: Float = 0f,
)

/** Default mayor / assistant / NPC roster; callers take only the first [maxCharacters]. */
val DefaultIdleHouseCharacters: List<IdleHouseCharacter> = listOf(
    IdleHouseCharacter(
        id = "mayor",
        role = CharacterRole.MAYOR,
        roomId = "living",
        nx = 0.62f,
        ny = 0.58f,
        phaseOffset = 0.0f,
    ),
    IdleHouseCharacter(
        id = "assistant",
        role = CharacterRole.ASSISTANT,
        roomId = "office",
        nx = 0.48f,
        ny = 0.55f,
        phaseOffset = 0.33f,
    ),
    IdleHouseCharacter(
        id = "npc_kitchen",
        role = CharacterRole.NPC,
        roomId = "kitchen",
        nx = 0.55f,
        ny = 0.62f,
        phaseOffset = 0.66f,
    ),
    IdleHouseCharacter(
        id = "npc_workshop",
        role = CharacterRole.NPC,
        roomId = "workshop",
        nx = 0.42f,
        ny = 0.58f,
        phaseOffset = 0.15f,
    ),
)
