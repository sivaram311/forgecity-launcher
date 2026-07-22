package buzz.delena.forgecity.assistant

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ForgeCityTtsDiagnosticsTest {
    @Before
    fun setUp() {
        ForgeCityTtsDiagnostics.clear()
    }

    @After
    fun tearDown() {
        ForgeCityTtsDiagnostics.clear()
    }

    @Test
    fun appendsWarnAndInfoWithLevels() {
        ForgeCityTtsDiagnostics.info("gemini_audio_start", "model=tts")
        ForgeCityTtsDiagnostics.warn("gemini_audio_timeout")
        val snap = ForgeCityTtsDiagnostics.snapshot.value
        assertTrue(snap.contains(" I gemini_audio_start model=tts"))
        assertTrue(snap.contains(" W gemini_audio_timeout"))
        assertEquals(2, ForgeCityTtsDiagnostics.lineCount())
    }

    @Test
    fun ringBufferCapsAtMaxLines() {
        repeat(ForgeCityTtsDiagnostics.MAX_LINES + 25) { i ->
            ForgeCityTtsDiagnostics.warn("event_$i")
        }
        assertEquals(ForgeCityTtsDiagnostics.MAX_LINES, ForgeCityTtsDiagnostics.lineCount())
        val snap = ForgeCityTtsDiagnostics.snapshot.value
        assertFalse(snap.contains("event_0"))
        assertTrue(snap.contains("event_${ForgeCityTtsDiagnostics.MAX_LINES + 24}"))
    }

    @Test
    fun clearEmptiesSnapshot() {
        ForgeCityTtsDiagnostics.warn("keep_me")
        ForgeCityTtsDiagnostics.clear()
        assertEquals("", ForgeCityTtsDiagnostics.snapshot.value)
        assertEquals(0, ForgeCityTtsDiagnostics.lineCount())
    }

    @Test
    fun uiStatusTruncates() {
        val long = "x".repeat(300)
        ForgeCityTtsDiagnostics.uiStatus(long)
        val snap = ForgeCityTtsDiagnostics.snapshot.value
        assertTrue(snap.contains("ui_status"))
        assertTrue(snap.length < 220)
    }
}
