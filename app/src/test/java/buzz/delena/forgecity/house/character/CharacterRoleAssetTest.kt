package buzz.delena.forgecity.house.character

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CharacterRoleAssetTest {
    @Test
    fun filamentAssetsAreDistinct() {
        assertEquals("filament/char_mayor.glb", CharacterRole.MAYOR.filamentAsset())
        assertEquals("filament/char_assist.glb", CharacterRole.ASSISTANT.filamentAsset())
        assertEquals("filament/char_npc.glb", CharacterRole.NPC.filamentAsset())
        val paths = DefaultIdleHouseCharacters.map { it.filamentAsset }.toSet()
        assertTrue(paths.size >= 3)
    }
}
