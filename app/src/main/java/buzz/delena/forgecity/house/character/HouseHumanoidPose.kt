package buzz.delena.forgecity.house.character

import kotlin.math.sin

/** Production-House-style actions for jointed capsule humanoids. */
enum class HumanoidAction {
    IDLE,
    TALK,
    WALK,
    SIT,
}

/**
 * Euler angles in **degrees** (SceneView [io.github.sceneview.math.Rotation]).
 * Root at feet y=0; local +Z forward — port of `production-house` Humanoid.tsx.
 */
data class HumanoidPose(
    val bodyY: Float,
    val bodyYawDeg: Float,
    val headPitchDeg: Float,
    val headYawDeg: Float,
    val armLXDeg: Float,
    val armLZDeg: Float,
    val armRXDeg: Float,
    val armRZDeg: Float,
    val legLXDeg: Float,
    val legRXDeg: Float,
)

data class HumanoidLook(
    val skin: Long,
    val hair: Long,
    val top: Long,
    val bottom: Long,
    val scale: Float = 1f,
)

object HouseHumanoidPose {
    const val HIP_Y = 0.92f

    fun lookFor(role: CharacterRole): HumanoidLook = when (role) {
        CharacterRole.MAYOR -> HumanoidLook(
            skin = 0xFFE8C4A0,
            hair = 0xFF2A1F14,
            top = 0xFFC9A227,
            bottom = 0xFF3D2E1F,
            scale = 1.05f,
        )
        CharacterRole.ASSISTANT -> HumanoidLook(
            skin = 0xFFF0D5C0,
            hair = 0xFF1A2430,
            top = 0xFF6B9BD1,
            bottom = 0xFF2C3A4A,
            scale = 0.96f,
        )
        CharacterRole.NPC -> HumanoidLook(
            skin = 0xFFD4A574,
            hair = 0xFF4A3020,
            top = 0xFF8F6A3E,
            bottom = 0xFF3A2A1C,
            scale = 1f,
        )
    }

    fun defaultAction(character: IdleHouseCharacter, assistantSpeaking: Boolean): HumanoidAction =
        HouseCharacterMotion.sample(character, timeSec = 0f, assistantSpeaking).action

    fun compute(action: HumanoidAction, timeSec: Float, phase: Float = 0f, intensity: Float = 1f): HumanoidPose {
        val t = timeSec + phase
        val i = intensity
        if (action == HumanoidAction.SIT) {
            return HumanoidPose(
                bodyY = sin(t * 1.0f) * 0.004f,
                bodyYawDeg = radToDeg(sin(t * 0.4f) * 0.02f),
                headPitchDeg = radToDeg(0.08f + sin(t * 1.5f) * 0.03f),
                headYawDeg = radToDeg(sin(t * 0.55f) * 0.1f),
                armLXDeg = radToDeg(0.35f),
                armLZDeg = radToDeg(-0.2f),
                armRXDeg = radToDeg(0.35f + sin(t * 1.2f) * 0.05f),
                armRZDeg = radToDeg(0.2f),
                legLXDeg = radToDeg(1.25f),
                legRXDeg = radToDeg(1.25f),
            )
        }
        val bodyY = if (action == HumanoidAction.WALK) {
            kotlin.math.abs(sin(t * 6f)) * 0.025f * i
        } else {
            sin(t * 1.2f) * 0.006f
        }
        val bodyYaw = radToDeg(
            if (action == HumanoidAction.TALK) sin(t * 1.4f) * 0.08f else sin(t * 0.5f) * 0.03f,
        )
        val headPitch = radToDeg(
            when (action) {
                HumanoidAction.TALK -> 0.06f + sin(t * 2f) * 0.04f
                else -> 0.04f
            },
        )
        val headYaw = radToDeg(sin(t * 0.7f) * if (action == HumanoidAction.IDLE) 0.08f else 0.12f)

        val walkSwing = sin(t * 6f) * 0.55f * i
        val (armRX, armRZ, armLX, armLZ) = when (action) {
            HumanoidAction.WALK -> Quad(
                radToDeg(walkSwing),
                radToDeg(0.08f),
                radToDeg(-walkSwing),
                radToDeg(-0.08f),
            )
            HumanoidAction.TALK -> Quad(
                radToDeg(-0.4f + sin(t * 2.5f) * 0.25f),
                radToDeg(0.15f),
                radToDeg(sin(t * 1.1f + 1f) * 0.08f),
                radToDeg(-0.12f),
            )
            HumanoidAction.IDLE, HumanoidAction.SIT -> Quad(
                radToDeg(sin(t * 1.1f) * 0.08f),
                radToDeg(0.12f),
                radToDeg(sin(t * 1.1f + 1f) * 0.08f),
                radToDeg(-0.12f),
            )
        }

        val (legL, legR) = if (action == HumanoidAction.WALK) {
            radToDeg(walkSwing * 0.9f) to radToDeg(-walkSwing * 0.9f)
        } else {
            radToDeg(0.02f) to radToDeg(0.02f)
        }

        return HumanoidPose(
            bodyY = bodyY,
            bodyYawDeg = bodyYaw,
            headPitchDeg = headPitch,
            headYawDeg = headYaw,
            armLXDeg = armLX,
            armLZDeg = armLZ,
            armRXDeg = armRX,
            armRZDeg = armRZ,
            legLXDeg = legL,
            legRXDeg = legR,
        )
    }

    private fun radToDeg(rad: Float): Float = rad * 57.29578f

    private data class Quad(val a: Float, val b: Float, val c: Float, val d: Float)
}
