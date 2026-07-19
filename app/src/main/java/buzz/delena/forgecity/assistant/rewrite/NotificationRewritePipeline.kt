package buzz.delena.forgecity.assistant.rewrite

import buzz.delena.forgecity.assistant.AssistantTtsEngine
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class NotificationRewritePipeline(
    private val client: AgentPortalRewriteClient,
    private val tts: AssistantTtsEngine,
    private val canProceed: () -> Boolean,
    private val endpoint: () -> String,
    private val apiKey: () -> String?,
) : AutoCloseable {
    private val queue = RewriteQueuePolicy(maxPending = 3)
    private val executor: ExecutorService = Executors.newSingleThreadExecutor { task ->
        Thread(task, "forgecity-tamil-rewrite").apply { isDaemon = true }
    }
    private var workerRunning = false
    private var closed = false

    @Synchronized
    fun enqueue(request: RewriteRequest) {
        if (closed) return
        queue.offer(request)
        if (!workerRunning) {
            workerRunning = true
            executor.execute(::drain)
        }
    }

    @Synchronized
    fun cancel(notificationKey: String) {
        if (closed) return
        val wasActive = queue.cancel(notificationKey)
        if (wasActive) client.cancel(notificationKey)
    }

    private fun drain() {
        while (true) {
            val request = synchronized(this) {
                if (closed) return
                queue.takeNext().also {
                    if (it == null) workerRunning = false
                }
            } ?: return

            try {
                if (!canProceed()) continue
                val key = apiKey()?.takeIf { it.isNotBlank() } ?: continue
                val configuredEndpoint = endpoint().takeIf { it.isNotBlank() } ?: continue
                val result = client.rewrite(configuredEndpoint, key, request)
                if (result !is RewriteResult.Success) continue
                if (!canProceed()) continue
                synchronized(this) {
                    if (closed || queue.activeKey != request.notificationKey) return@synchronized
                    tts.speakTamil(result.tamilText)
                }
            } finally {
                synchronized(this) { queue.complete(request.notificationKey) }
            }
        }
    }

    @Synchronized
    override fun close() {
        if (closed) return
        closed = true
        queue.clear()
        client.close()
        executor.shutdownNow()
    }
}
