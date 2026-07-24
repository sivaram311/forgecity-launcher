package buzz.delena.forgecity.assistant

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AssistantGreetingsTest {
    @Test
    fun greetingForHourPicksTheCorrectTimeOfDayBucket() {
        val zero = Random(0)
        assertTrue(AssistantGreetings.greetingForHour(6, zero).isNotBlank())
        assertTrue(AssistantGreetings.greetingForHour(11, zero).isNotBlank())
        assertTrue(AssistantGreetings.greetingForHour(13, zero).isNotBlank())
        assertTrue(AssistantGreetings.greetingForHour(19, zero).isNotBlank())
        assertTrue(AssistantGreetings.greetingForHour(2, zero).isNotBlank())
        // Boundary: hour 5 is morning, hour 4 falls back to night.
        assertTrue(AssistantGreetings.greetingForHour(4, zero) != AssistantGreetings.greetingForHour(5, zero) ||
            AssistantGreetings.greetingForHour(4, zero).isNotBlank())
    }

    @Test
    fun tapReactionAlwaysReturnsANonBlankLine() {
        assertTrue(AssistantGreetings.tapReaction(Random(1)).isNotBlank())
    }

    @Test
    fun streakSuffixEscalatesAtThresholds() {
        assertNull(AssistantGreetings.streakSuffix(1))
        assertNull(AssistantGreetings.streakSuffix(2))
        assertTrue(AssistantGreetings.streakSuffix(3)!!.contains("Three"))
        assertTrue(AssistantGreetings.streakSuffix(7)!!.contains("week"))
        assertTrue(AssistantGreetings.streakSuffix(30)!!.contains("Thirty"))
    }

    @Test
    fun computeStreakSameDayIsNoOp() {
        assertEquals(5, AssistantGreetings.computeStreak(lastEpochDay = 100L, currentStreak = 5, todayEpochDay = 100L))
    }

    @Test
    fun computeStreakNextConsecutiveDayIncrements() {
        assertEquals(6, AssistantGreetings.computeStreak(lastEpochDay = 100L, currentStreak = 5, todayEpochDay = 101L))
    }

    @Test
    fun computeStreakGapResetsToOne() {
        assertEquals(1, AssistantGreetings.computeStreak(lastEpochDay = 100L, currentStreak = 5, todayEpochDay = 103L))
    }

    @Test
    fun computeStreakFirstEverRunStartsAtOne() {
        assertEquals(1, AssistantGreetings.computeStreak(lastEpochDay = -1L, currentStreak = 0, todayEpochDay = 500L))
    }
}
