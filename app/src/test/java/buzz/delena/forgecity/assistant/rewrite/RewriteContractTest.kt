package buzz.delena.forgecity.assistant.rewrite

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RewriteResponseParserTest {
    @Test
    fun acceptsStrictTamilResponse() {
        val result = RewriteResponseParser.parse(
            """{"schemaVersion":1,"status":"ok","tamil":"உங்களுக்கு ஒரு புதிய செய்தி வந்துள்ளது."}""",
        )
        assertTrue(result is RewriteResult.Success)
    }

    @Test
    fun rejectsWrongSchemaEnglishAndMetaLeakage() {
        assertEquals(
            RewriteResult.Malformed,
            RewriteResponseParser.parse("""{"schemaVersion":1,"status":"done","tamil":"தமிழ் செய்தி"}"""),
        )
        assertEquals(
            RewriteResult.Malformed,
            RewriteResponseParser.parse("""{"schemaVersion":1,"status":"ok","tamil":"New message"}"""),
        )
        assertEquals(
            RewriteResult.Malformed,
            RewriteResponseParser.parse(
                """{"schemaVersion":1,"status":"ok","tamil":"Tamil: தமிழ் செய்தி"}""",
            ),
        )
        assertEquals(
            RewriteResult.Malformed,
            RewriteResponseParser.parse(
                """{"schemaVersion":1,"status":"ok","tamil":"தமிழ் செய்தி","extra":true}""",
            ),
        )
        assertEquals(
            RewriteResult.Malformed,
            RewriteResponseParser.parse(
                """{"status":"ok","tamilText":"தமிழ் செய்தி"}""",
            ),
        )
    }

    @Test
    fun rejectsOverLimit() {
        val text = "அ".repeat(RewriteResponseParser.MAX_TAMIL_CHARS + 1)
        assertEquals(
            RewriteResult.Malformed,
            RewriteResponseParser.parse("""{"schemaVersion":1,"status":"ok","tamil":"$text"}"""),
        )
    }
}

class RewriteRequestTest {
    @Test
    fun includesNoStoreAndEscapesNotificationText() {
        val json = RewriteRequest(
            notificationKey = "memory-only",
            appLabel = "Chat",
            title = "A \"title\"",
            body = "line one\nline two",
        ).toJson()

        assertTrue(json.contains("\"store\":false"))
        assertTrue(json.contains("""A \"title\""""))
        assertTrue(json.contains("""line one\nline two"""))
        assertFalse(json.contains("memory-only"))
    }
}

class RewriteQueuePolicyTest {
    @Test
    fun boundsPendingAndDropsOldest() {
        val queue = RewriteQueuePolicy(maxPending = 3)
        queue.offer(request("active"))
        assertEquals("active", queue.takeNext()?.notificationKey)
        queue.offer(request("a"))
        queue.offer(request("b"))
        queue.offer(request("c"))
        val result = queue.offer(request("d"))

        assertEquals(OfferResult.DroppedOldest("a"), result)
        assertEquals(listOf("b", "c", "d"), queue.pendingKeys())
    }

    @Test
    fun coalescesPendingAndActiveKeys() {
        val queue = RewriteQueuePolicy(maxPending = 3)
        queue.offer(request("active"))
        queue.takeNext()
        assertEquals(OfferResult.Coalesced, queue.offer(request("active")))

        queue.offer(request("same", body = "old"))
        assertEquals(OfferResult.Coalesced, queue.offer(request("same", body = "new")))
        queue.complete("active")
        assertEquals("new", queue.takeNext()?.body)
    }

    @Test
    fun cancellationRemovesPendingAndActive() {
        val queue = RewriteQueuePolicy(maxPending = 3)
        queue.offer(request("active"))
        queue.takeNext()
        queue.offer(request("pending"))

        assertTrue(queue.cancel("active"))
        queue.cancel("pending")
        assertEquals(emptyList<String>(), queue.pendingKeys())
    }

    private fun request(key: String, body: String = key) = RewriteRequest(
        notificationKey = key,
        appLabel = "Chat",
        title = "Title",
        body = body,
    )
}
