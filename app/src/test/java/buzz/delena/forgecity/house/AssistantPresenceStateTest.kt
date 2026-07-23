package buzz.delena.forgecity.house

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AssistantPresenceStateTest {
    @Test
    fun markSpeakingIsTrueUntilWindowEnds() {
        var now = 1_000L
        val state = AssistantPresenceState { now }

        assertFalse(state.isSpeaking())
        state.markSpeaking(500L)
        assertTrue(state.isSpeaking())
        assertEquals(500L, state.remainingMs())

        now = 1_499L
        assertTrue(state.isSpeaking())

        now = 1_500L
        assertFalse(state.isSpeaking())
        assertEquals(0L, state.remainingMs())
    }

    @Test
    fun markSpeakingExtendsRatherThanShortens() {
        var now = 0L
        val state = AssistantPresenceState { now }

        state.markSpeaking(1_000L)
        now = 200L
        state.markSpeaking(100L) // would end at 300 if replaced
        assertTrue(state.isSpeaking(nowMs = 900L))
        assertFalse(state.isSpeaking(nowMs = 1_000L))
    }

    @Test
    fun clearStopsSpeakingImmediately() {
        val state = AssistantPresenceState { 50L }
        state.markSpeaking(10_000L)
        assertTrue(state.isSpeaking())
        state.clear()
        assertFalse(state.isSpeaking())
    }

    @Test
    fun negativeDurationIsTreatedAsZero() {
        var now = 10L
        val state = AssistantPresenceState { now }
        state.markSpeaking(-5L)
        assertFalse(state.isSpeaking())
        now = 10L
        assertEquals(0L, state.remainingMs())
    }
}
