package buzz.delena.forgecity.assistant

import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
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

class AssistantSpeechModeTest {
    @Test
    fun migratesLegacyFlagsToOneExplicitMode() {
        assertEquals(
            AssistantSpeechMode.AGENT_PORTAL_TAMIL,
            AssistantSpeechMode.migrate(ttsEnabled = true, remoteRewriteEnabled = true),
        )
        assertEquals(
            AssistantSpeechMode.DIRECT_TTS,
            AssistantSpeechMode.migrate(ttsEnabled = true, remoteRewriteEnabled = false),
        )
        assertEquals(
            AssistantSpeechMode.OFF,
            AssistantSpeechMode.migrate(ttsEnabled = false, remoteRewriteEnabled = true),
        )
    }

    @Test
    fun modeCycleIsStable() {
        assertEquals(AssistantSpeechMode.DIRECT_TTS, AssistantSpeechMode.OFF.next())
        assertEquals(
            AssistantSpeechMode.AGENT_PORTAL_TAMIL,
            AssistantSpeechMode.DIRECT_TTS.next(),
        )
        assertEquals(
            AssistantSpeechMode.SMART_CASCADE,
            AssistantSpeechMode.AGENT_PORTAL_TAMIL.next(),
        )
        assertEquals(AssistantSpeechMode.OFF, AssistantSpeechMode.SMART_CASCADE.next())
    }

    @Test
    fun cascadeRouteAlwaysAvailable() {
        assertEquals(
            NotificationSpeechRoute.SMART_CASCADE,
            NotificationSpeechRoute.resolve(
                AssistantSpeechMode.SMART_CASCADE,
                portalConfigured = false,
            ),
        )
    }

    @Test
    fun routesDirectWithoutPortalAndPortalOnlyWhenConfigured() {
        assertEquals(
            NotificationSpeechRoute.DIRECT,
            NotificationSpeechRoute.resolve(
                AssistantSpeechMode.DIRECT_TTS,
                portalConfigured = false,
            ),
        )
        assertEquals(
            NotificationSpeechRoute.NONE,
            NotificationSpeechRoute.resolve(
                AssistantSpeechMode.AGENT_PORTAL_TAMIL,
                portalConfigured = false,
            ),
        )
        assertEquals(
            NotificationSpeechRoute.AGENT_PORTAL_TAMIL,
            NotificationSpeechRoute.resolve(
                AssistantSpeechMode.AGENT_PORTAL_TAMIL,
                portalConfigured = true,
            ),
        )
    }

    @Test
    fun launcherChromeDefaultsHidden() {
        assertFalse(LauncherChromeDefaults.VISIBLE)
    }
}
