package buzz.delena.forgecity.house.character

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HouseCharacterMotionTest {
    @Test
    fun mayorPatrolsAndMoves() {
        val mayor = DefaultIdleHouseCharacters.first { it.id == "mayor" }
        val a = HouseCharacterMotion.sample(mayor, 0.5f, false)
        val b = HouseCharacterMotion.sample(mayor, 5.0f, false)
        assertTrue(a.action == HumanoidAction.WALK || a.action == HumanoidAction.IDLE)
        // Over a few seconds the patrol should change normalized position.
        assertTrue(a.nx != b.nx || a.nz != b.nz)
    }

    @Test
    fun workshopPatrols() {
        val npc = DefaultIdleHouseCharacters.first { it.id == "npc_workshop" }
        assertEquals(CharacterMotionMode.PATROL, HouseCharacterMotion.profileFor(npc).mode)
        val s = HouseCharacterMotion.sample(npc, 2f, false)
        assertTrue(s.nx in 0.2f..0.9f)
    }

    @Test
    fun kitchenSits() {
        val npc = DefaultIdleHouseCharacters.first { it.id == "npc_kitchen" }
        val s = HouseCharacterMotion.sample(npc, 1f, false)
        assertEquals(HumanoidAction.SIT, s.action)
        assertTrue(s.y > 0.3f)
    }
}
