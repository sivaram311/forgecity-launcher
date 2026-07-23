package buzz.delena.forgecity.house

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * House-facing assistant presence: [speaking] for Compose / HomeScreen.
 *
 * Call [pulseSpeech] / [onSpeakStarted] from TTS [SpeakResult.STARTED] (no notification text).
 */
object AssistantHouseBridge {
    private val presence = AssistantPresenceState()
    private val _speaking = MutableStateFlow(false)
    val speaking: StateFlow<Boolean> = _speaking.asStateFlow()

    /** Snapshot for `assistantSpeaking: Boolean` props. */
    val assistantSpeaking: Boolean
        get() = _speaking.value

    private val clearGeneration = AtomicInteger(0)
    private var clearFuture: ScheduledFuture<*>? = null
    private val clearExecutor = Executors.newSingleThreadScheduledExecutor { task ->
        Thread(task, "forgecity-assistant-presence").apply { isDaemon = true }
    }

    /** Pulse the character for [durationMs] (auto-clears). */
    @JvmOverloads
    fun pulseSpeech(durationMs: Long = AssistantPresenceState.DEFAULT_SPEAK_MS) {
        presence.markSpeaking(durationMs)
        publish()
        scheduleClear(presence.remainingMs())
    }

    /** Alias for TTS engine / orchestrator when speech starts. */
    fun onSpeakStarted(durationMs: Long = AssistantPresenceState.DEFAULT_SPEAK_MS) =
        pulseSpeech(durationMs)

    fun markSpeaking(durationMs: Long = AssistantPresenceState.DEFAULT_SPEAK_MS) =
        pulseSpeech(durationMs)

    fun clear() {
        clearGeneration.incrementAndGet()
        clearFuture?.cancel(false)
        clearFuture = null
        presence.clear()
        publish()
    }

    private fun publish() {
        _speaking.value = presence.isSpeaking()
    }

    private fun scheduleClear(delayMs: Long) {
        val gen = clearGeneration.incrementAndGet()
        clearFuture?.cancel(false)
        if (delayMs <= 0L) {
            presence.clear()
            publish()
            return
        }
        clearFuture = clearExecutor.schedule(
            {
                if (gen == clearGeneration.get()) {
                    presence.clear()
                    publish()
                }
            },
            delayMs,
            TimeUnit.MILLISECONDS,
        )
    }
}
