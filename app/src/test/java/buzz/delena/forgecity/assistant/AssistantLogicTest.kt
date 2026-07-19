package buzz.delena.forgecity.assistant

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuietHoursTest {
    @Test
    fun sameWindowIsNeverQuiet() {
        assertFalse(QuietHours.isQuiet(100, 100, 100))
    }

    @Test
    fun wrapMidnightQuiet() {
        assertTrue(QuietHours.isQuiet(23 * 60, 22 * 60, 7 * 60))
        assertTrue(QuietHours.isQuiet(3 * 60, 22 * 60, 7 * 60))
        assertFalse(QuietHours.isQuiet(12 * 60, 22 * 60, 7 * 60))
    }
}

class NotificationSpeechFilterTest {
    @Test
    fun denyByDefaultEmptyAllowlist() {
        assertFalse(
            NotificationSpeechFilter.shouldSpeak(
                packageName = "com.chat",
                ownPackage = "buzz.delena.forgecity",
                allowedPackages = emptySet(),
                title = "Hi",
                text = "Hello",
                isOngoing = false,
                isGroupSummary = false,
                isForegroundService = false,
            ),
        )
    }

    @Test
    fun allowlistedNonOngoingSpeaks() {
        assertTrue(
            NotificationSpeechFilter.shouldSpeak(
                packageName = "com.chat",
                ownPackage = "buzz.delena.forgecity",
                allowedPackages = setOf("com.chat"),
                title = "Hi",
                text = "Hello",
                isOngoing = false,
                isGroupSummary = false,
                isForegroundService = false,
            ),
        )
    }

    @Test
    fun skipsOngoingAndSummary() {
        assertFalse(
            NotificationSpeechFilter.shouldSpeak(
                packageName = "com.chat",
                ownPackage = "buzz.delena.forgecity",
                allowedPackages = setOf("com.chat"),
                title = "Hi",
                text = "Hello",
                isOngoing = true,
                isGroupSummary = false,
                isForegroundService = false,
            ),
        )
    }
}

class NotificationDedupeTest {
    @Test
    fun suppressesWithinTtl() {
        var now = 1_000L
        val dedupe = NotificationDedupe(ttlMs = 3_000L, clock = { now })
        assertTrue(dedupe.shouldProcess("a"))
        assertFalse(dedupe.shouldProcess("a"))
        now += 4_000L
        assertTrue(dedupe.shouldProcess("a"))
    }
}
