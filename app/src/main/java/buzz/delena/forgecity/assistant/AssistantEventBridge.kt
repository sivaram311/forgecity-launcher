package buzz.delena.forgecity.assistant

import android.app.PendingIntent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class AssistantUiEvent(
    val packageName: String,
    val appLabel: String,
    val title: String,
    val shortText: String,
    val notificationKey: String,
    /** Ephemeral system action; never persisted or logged. */
    val contentIntent: PendingIntent? = null,
)

object AssistantEventBridge {
    private val _events = MutableSharedFlow<AssistantUiEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<AssistantUiEvent> = _events.asSharedFlow()

    fun emit(event: AssistantUiEvent) {
        _events.tryEmit(event)
    }
}
