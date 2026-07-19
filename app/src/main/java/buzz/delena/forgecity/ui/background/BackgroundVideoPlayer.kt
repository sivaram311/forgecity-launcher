@file:androidx.annotation.OptIn(
    markerClass = [androidx.media3.common.util.UnstableApi::class],
)

package buzz.delena.forgecity.ui.background

import android.content.Context
import android.net.Uri
import androidx.annotation.RawRes
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer

class BackgroundVideoPlayer(
    context: Context,
    @RawRes rawResourceId: Int,
    private val onPlaybackError: (PlaybackException) -> Unit,
) {
    val player: ExoPlayer
    private var released = false

    init {
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                1_000,
                3_000,
                250,
                500,
            )
            .build()

        player = ExoPlayer.Builder(context.applicationContext)
            .setLoadControl(loadControl)
            .build()
            .apply {
                volume = 0f
                repeatMode = Player.REPEAT_MODE_ALL
                playWhenReady = false
                addListener(
                    object : Player.Listener {
                        override fun onPlayerError(error: PlaybackException) {
                            onPlaybackError(error)
                        }
                    },
                )
                setMediaItem(
                    MediaItem.fromUri(
                        Uri.parse(
                            "android.resource://${context.packageName}/$rawResourceId",
                        ),
                    ),
                )
                prepare()
            }
    }

    fun resume() {
        if (!released) player.play()
    }

    fun pause() {
        if (!released) player.pause()
    }

    fun release() {
        if (released) return
        released = true
        player.release()
    }
}
