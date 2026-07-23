package buzz.delena.forgecity.house.character

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HouseHumanoidPoseTest {
    @Test
    fun idleAndWalkDiffer() {
        val idle = HouseHumanoidPose.compute(HumanoidAction.IDLE, 1.5f)
        val walk = HouseHumanoidPose.compute(HumanoidAction.WALK, 1.5f)
        assertNotEquals(idle.armRXDeg, walk.armRXDeg, 0.01f)
        assertTrue(kotlin.math.abs(walk.legLXDeg) > kotlin.math.abs(idle.legLXDeg))
    }

    @Test
    fun talkMovesRightArm() {
        val talk = HouseHumanoidPose.compute(HumanoidAction.TALK, 0.8f)
        assertTrue(kotlin.math.abs(talk.armRXDeg) > 5f)
    }

    @Test
    fun assistantSitsOrTalks() {
        val assist = DefaultIdleHouseCharacters.first { it.role == CharacterRole.ASSISTANT }
        assertEquals(
            HumanoidAction.TALK,
            HouseHumanoidPose.defaultAction(assist, assistantSpeaking = true),
        )
        assertEquals(
            HumanoidAction.SIT,
            HouseHumanoidPose.defaultAction(assist, assistantSpeaking = false),
        )
    }

    @Test
    fun sitFoldsLegs() {
        val sit = HouseHumanoidPose.compute(HumanoidAction.SIT, 0.5f)
        assertTrue(sit.legLXDeg > 40f)
    }

    @Test
    fun looksDistinctPerRole() {
        val looks = CharacterRole.entries.map { HouseHumanoidPose.lookFor(it).top }.toSet()
        assertEquals(3, looks.size)
    }
}
