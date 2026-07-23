package buzz.delena.forgecity.house.character

/**
 * House occupant roster. Filament HOME uses jointed capsule humanoids (0.11+);
 * Compose [buzz.delena.forgecity.ui.house.HouseHomeSurface] still draws stylized 2D.
 * Positions are normalized inside the target [roomId] floor cell (0–1).
 */
enum class CharacterRole {
    MAYOR,
    ASSISTANT,
    NPC,
}

/** Filament asset under `assets/filament/` (Grok 0.10.6 character fidelity). */
fun CharacterRole.filamentAsset(): String = when (this) {
    CharacterRole.MAYOR -> "filament/char_mayor.glb"
    CharacterRole.ASSISTANT -> "filament/char_assist.glb"
    CharacterRole.NPC -> "filament/char_npc.glb"
}

data class IdleHouseCharacter(
    val id: String,
    val role: CharacterRole,
    val roomId: String,
    val nx: Float,
    val ny: Float,
    /** Phase offset so bob/breath desyncs across characters. */
    val phaseOffset: Float = 0f,
) {
    val filamentAsset: String get() = role.filamentAsset()
}

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
