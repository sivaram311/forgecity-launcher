package buzz.delena.forgecity.ui.assistant

import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import buzz.delena.forgecity.house.character.CharacterRole
import buzz.delena.forgecity.house.character.HouseHumanoidPose
import buzz.delena.forgecity.house.character.HumanoidAction
import buzz.delena.forgecity.ui.house.HouseHumanoidNode
import com.google.android.filament.LightManager
import io.github.sceneview.RenderQuality
import io.github.sceneview.SceneView
import io.github.sceneview.SurfaceType
import io.github.sceneview.createEnvironment
import io.github.sceneview.math.Direction
import io.github.sceneview.math.Position
import io.github.sceneview.math.colorOf
import io.github.sceneview.rememberCameraManipulator
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberEnvironment
import io.github.sceneview.rememberEnvironmentLoader
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberView

private val TARGET_POSITION = Position(x = 0f, y = 1.0f, z = 0f)

/**
 * Assistant home mode: one full-screen animated character. Standalone — does
 * NOT reuse [buzz.delena.forgecity.ui.house.HouseFilamentSurface]'s
 * room/orbit-camera/hotspot machinery, which is house-space-specific.
 * [action] drives idle/talk/wave; tapping the character calls [onTap].
 */
@Composable
fun AssistantCharacterScreen(
    action: HumanoidAction,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var timeSec by remember { mutableFloatStateOf(0f) }

    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val view = rememberView(engine)
    val environmentLoader = rememberEnvironmentLoader(engine)
    val environment = rememberEnvironment(
        environmentLoader = environmentLoader,
        environment = remember(environmentLoader) {
            { createEnvironment(environmentLoader, isOpaque = false) }
        },
    )

    val cameraNode = rememberCameraNode(engine) {
        position = Position(x = 0f, y = 1.15f, z = 2.3f)
        lookAt(TARGET_POSITION)
    }
    val cameraManipulator = rememberCameraManipulator(
        orbitHomePosition = cameraNode.worldPosition,
        targetPosition = TARGET_POSITION,
    )

    val mainLight = rememberMainLightNode(engine) {
        intensity = 110_000f
        lightDirection = Direction(-0.4f, -0.8f, -0.4f)
        color = colorOf(Color(0xFFFFF2DC))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF2A3B52), Color(0xFF16202E)))),
    ) {
        SceneView(
            modifier = Modifier.fillMaxSize(),
            surfaceType = SurfaceType.TextureSurface,
            engine = engine,
            modelLoader = modelLoader,
            view = view,
            environment = environment,
            mainLightNode = mainLight,
            fillLightNode = null,
            cameraNode = cameraNode,
            cameraManipulator = cameraManipulator,
            renderQuality = RenderQuality.Default,
            autoCenterContent = false,
            autoFitContent = false,
            isOpaque = false,
            onFrame = { nanos ->
                val t = nanos / 1_000_000_000f
                if (t - timeSec >= 0.033f) timeSec = t
            },
            onTouchEvent = { event, _ ->
                if (event.action != MotionEvent.ACTION_UP) return@SceneView false
                onTap()
                true
            },
        ) {
            LightNode(
                type = LightManager.Type.DIRECTIONAL,
                intensity = 24_000f,
                direction = Direction(0.5f, -0.3f, 0.6f),
                color = colorOf(Color(0xFF9FD3FF)),
            )
            val pose = HouseHumanoidPose.compute(action = action, timeSec = timeSec)
            HouseHumanoidNode(
                look = HouseHumanoidPose.lookFor(CharacterRole.ASSISTANT),
                pose = pose,
                worldPosition = Position(x = 0f, y = 0f, z = 0f),
                nodeName = "assistant_character",
            )
        }
    }
}
