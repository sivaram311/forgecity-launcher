package buzz.delena.forgecity.house.character

import kotlin.math.atan2
import kotlin.math.floor
/** How an occupant spends time in their room. */
enum class CharacterMotionMode {
    /** Loop waypoints at walking speed. */
    PATROL,
    /** Seated at home spot (desk / stool). */
    SIT,
    /** Hold spawn nx/ny with idle sway. */
    IDLE_SPOT,
}

data class CharacterMotionProfile(
    val mode: CharacterMotionMode,
    /** Normalized room waypoints (nx, nz); used for PATROL. */
    val waypoints: List<Pair<Float, Float>> = emptyList(),
    val periodSec: Float = 12f,
    /** Floor Y offset when sitting (chair seat). */
    val sitY: Float = 0.48f,
)

data class CharacterMotionSample(
    val nx: Float,
    val nz: Float,
    val y: Float,
    val yawDeg: Float,
    val action: HumanoidAction,
)

/**
 * Room patrols / sit loops (gap backlog #7 — 0.12).
 * Pure math; [HouseFilamentSurface] maps into world meters.
 */
object HouseCharacterMotion {
    fun profileFor(character: IdleHouseCharacter): CharacterMotionProfile = when (character.id) {
        "mayor" -> CharacterMotionProfile(
            mode = CharacterMotionMode.PATROL,
            waypoints = listOf(
                0.55f to 0.45f,
                0.78f to 0.55f,
                0.62f to 0.78f,
                0.38f to 0.58f,
            ),
            periodSec = 16f,
        )
        "assistant" -> CharacterMotionProfile(
            mode = CharacterMotionMode.SIT,
            sitY = 0.52f,
        )
        "npc_kitchen" -> CharacterMotionProfile(
            mode = CharacterMotionMode.SIT,
            sitY = 0.45f,
        )
        "npc_workshop" -> CharacterMotionProfile(
            mode = CharacterMotionMode.PATROL,
            waypoints = listOf(
                0.35f to 0.40f,
                0.70f to 0.45f,
                0.65f to 0.75f,
                0.40f to 0.70f,
            ),
            periodSec = 11f,
        )
        else -> CharacterMotionProfile(mode = CharacterMotionMode.IDLE_SPOT)
    }

    fun sample(
        character: IdleHouseCharacter,
        timeSec: Float,
        assistantSpeaking: Boolean,
    ): CharacterMotionSample {
        val profile = profileFor(character)
        val phase = character.phaseOffset * profile.periodSec

        if (character.role == CharacterRole.ASSISTANT && assistantSpeaking) {
            return CharacterMotionSample(
                nx = character.nx,
                nz = character.ny,
                y = profile.sitY,
                yawDeg = 0f,
                action = HumanoidAction.TALK,
            )
        }

        return when (profile.mode) {
            CharacterMotionMode.SIT -> CharacterMotionSample(
                nx = character.nx,
                nz = character.ny,
                y = profile.sitY,
                yawDeg = 0f,
                action = HumanoidAction.SIT,
            )
            CharacterMotionMode.IDLE_SPOT -> CharacterMotionSample(
                nx = character.nx,
                nz = character.ny,
                y = 0f,
                yawDeg = 0f,
                action = HumanoidAction.IDLE,
            )
            CharacterMotionMode.PATROL -> patrolSample(character, profile, timeSec + phase)
        }
    }

    private fun patrolSample(
        character: IdleHouseCharacter,
        profile: CharacterMotionProfile,
        t: Float,
    ): CharacterMotionSample {
        val wps = profile.waypoints
        if (wps.size < 2) {
            return CharacterMotionSample(character.nx, character.ny, 0f, 0f, HumanoidAction.IDLE)
        }
        val n = wps.size
        val period = profile.periodSec.coerceAtLeast(4f)
        // Fraction of loop; pause briefly at each corner (~15% of segment).
        val u = ((t % period) + period) % period / period
        val segF = u * n
        val i = floor(segF).toInt().coerceIn(0, n - 1)
        val local = segF - i
        val a = wps[i]
        val b = wps[(i + 1) % n]
        val pause = 0.18f
        val (nx, nz, moving) = if (local < pause) {
            Triple(a.first, a.second, false)
        } else {
            val s = ((local - pause) / (1f - pause)).coerceIn(0f, 1f)
            // Smoothstep
            val s2 = s * s * (3f - 2f * s)
            Triple(
                a.first + (b.first - a.first) * s2,
                a.second + (b.second - a.second) * s2,
                true,
            )
        }
        val dx = b.first - a.first
        val dz = b.second - a.second
        val yaw = Math.toDegrees(atan2(dx.toDouble(), dz.toDouble())).toFloat()
        return CharacterMotionSample(
            nx = nx,
            nz = nz,
            y = 0f,
            yawDeg = yaw,
            action = if (moving) HumanoidAction.WALK else HumanoidAction.IDLE,
        )
    }
}
