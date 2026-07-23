package buzz.delena.forgecity.ui.house

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import buzz.delena.forgecity.house.character.HouseHumanoidPose
import buzz.delena.forgecity.house.character.HumanoidLook
import buzz.delena.forgecity.house.character.HumanoidPose

/**
 * Jointed capsule humanoid (Production House Humanoid.tsx port) for SceneScope.
 * Capsule torso/limbs + sphere head/hair; pose from [HouseHumanoidPose].
 */
@Composable
fun io.github.sceneview.SceneScope.HouseHumanoidNode(
    look: HumanoidLook,
    pose: HumanoidPose,
    worldPosition: Position,
    nodeName: String,
    yawDeg: Float = 0f,
) {
    val skinMat = remember(nodeName, look.skin) {
        materialLoader.createColorInstance(Color(look.skin), metallic = 0.02f, roughness = 0.75f)
    }
    val hairMat = remember(nodeName, look.hair) {
        materialLoader.createColorInstance(Color(look.hair), metallic = 0f, roughness = 0.9f)
    }
    val topMat = remember(nodeName, look.top) {
        materialLoader.createColorInstance(Color(look.top), metallic = 0.05f, roughness = 0.7f)
    }
    val bottomMat = remember(nodeName, look.bottom) {
        materialLoader.createColorInstance(Color(look.bottom), metallic = 0.02f, roughness = 0.8f)
    }

    val hipY = HouseHumanoidPose.HIP_Y
    Node(
        position = worldPosition,
        rotation = Rotation(y = yawDeg),
        scale = Scale(look.scale),
        apply = { name = nodeName },
    ) {
        Node(
            position = Position(y = pose.bodyY),
            rotation = Rotation(y = pose.bodyYawDeg),
        ) {
            // Torso
            CapsuleNode(
                radius = 0.18f,
                height = 0.42f,
                materialInstance = topMat,
                position = Position(y = hipY + 0.35f),
            )
            // Head + hair
            Node(
                position = Position(y = hipY + 0.72f),
                rotation = Rotation(x = pose.headPitchDeg, y = pose.headYawDeg),
            ) {
                SphereNode(radius = 0.13f, materialInstance = skinMat)
                SphereNode(
                    radius = 0.135f,
                    materialInstance = hairMat,
                    position = Position(y = 0.1f),
                )
            }
            // Arms (pivot at shoulder; capsule hangs down)
            Node(
                position = Position(x = 0.28f, y = hipY + 0.5f),
                rotation = Rotation(x = pose.armRXDeg, z = pose.armRZDeg),
            ) {
                CapsuleNode(
                    radius = 0.05f,
                    height = 0.34f,
                    materialInstance = topMat,
                    position = Position(y = -0.22f),
                )
                SphereNode(
                    radius = 0.045f,
                    materialInstance = skinMat,
                    position = Position(y = -0.48f),
                )
            }
            Node(
                position = Position(x = -0.28f, y = hipY + 0.5f),
                rotation = Rotation(x = pose.armLXDeg, z = pose.armLZDeg),
            ) {
                CapsuleNode(
                    radius = 0.05f,
                    height = 0.34f,
                    materialInstance = topMat,
                    position = Position(y = -0.22f),
                )
                SphereNode(
                    radius = 0.045f,
                    materialInstance = skinMat,
                    position = Position(y = -0.48f),
                )
            }
            // Legs
            Node(
                position = Position(x = 0.1f, y = hipY),
                rotation = Rotation(x = pose.legRXDeg),
            ) {
                CapsuleNode(
                    radius = 0.07f,
                    height = 0.55f,
                    materialInstance = bottomMat,
                    position = Position(y = -0.4f),
                )
            }
            Node(
                position = Position(x = -0.1f, y = hipY),
                rotation = Rotation(x = pose.legLXDeg),
            ) {
                CapsuleNode(
                    radius = 0.07f,
                    height = 0.55f,
                    materialInstance = bottomMat,
                    position = Position(y = -0.4f),
                )
            }
        }
    }
}
