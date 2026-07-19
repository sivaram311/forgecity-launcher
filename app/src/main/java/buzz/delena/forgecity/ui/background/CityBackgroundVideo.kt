@file:androidx.annotation.OptIn(
    markerClass = [androidx.media3.common.util.UnstableApi::class],
)

package buzz.delena.forgecity.ui.background

import android.view.LayoutInflater
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.ui.PlayerView
import buzz.delena.forgecity.R

@Composable
fun CityBackgroundVideo(
    enabled: Boolean,
    modifier: Modifier = Modifier,
    opacity: Float = 1f,
    onPlaybackError: (Throwable) -> Unit = {},
    fallback: @Composable () -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnPlaybackError by rememberUpdatedState(onPlaybackError)
    val rawResourceId = remember(context) {
        context.resources.getIdentifier(
            "city_background",
            "raw",
            context.packageName,
        )
    }
    var playbackFailed by remember(enabled, rawResourceId) {
        mutableStateOf(false)
    }
    val shouldCreatePlayer = enabled && rawResourceId != 0 && !playbackFailed
    val videoPlayer = remember(context.applicationContext, rawResourceId, shouldCreatePlayer) {
        if (shouldCreatePlayer) {
            BackgroundVideoPlayer(
                context = context.applicationContext,
                rawResourceId = rawResourceId,
                onPlaybackError = { error ->
                    playbackFailed = true
                    currentOnPlaybackError(error)
                },
            )
        } else {
            null
        }
    }

    DisposableEffect(videoPlayer) {
        onDispose {
            videoPlayer?.release()
        }
    }

    DisposableEffect(lifecycleOwner, videoPlayer, enabled) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> if (enabled) videoPlayer?.resume()
                Lifecycle.Event.ON_PAUSE,
                Lifecycle.Event.ON_STOP,
                -> videoPlayer?.pause()
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        if (enabled && lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            videoPlayer?.resume()
        }

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            videoPlayer?.pause()
        }
    }

    if (videoPlayer == null) {
        fallback()
        return
    }

    val fade = remember(videoPlayer) { Animatable(0f) }
    LaunchedEffect(videoPlayer) {
        fade.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 450),
        )
    }
    val clampedOpacity = if (opacity.isFinite()) {
        opacity.coerceIn(0.4f, 1f)
    } else {
        1f
    }

    AndroidView(
        factory = { viewContext ->
            (LayoutInflater.from(viewContext).inflate(
                R.layout.view_city_background_video,
                null,
                false,
            ) as PlayerView).apply {
                player = videoPlayer.player
                keepScreenOn = false
            }
        },
        update = { view ->
            if (view.player !== videoPlayer.player) {
                view.player = videoPlayer.player
            }
        },
        modifier = modifier.graphicsLayer {
            alpha = clampedOpacity * fade.value
        },
    )
}
