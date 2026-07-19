package buzz.delena.forgecity.assistant.rewrite

class RewriteQueuePolicy(private val maxPending: Int = 3) {
    private val pending = ArrayDeque<RewriteRequest>()
    var activeKey: String? = null
        private set

    val pendingCount: Int
        get() = pending.size

    fun offer(request: RewriteRequest): OfferResult {
        if (request.notificationKey == activeKey) return OfferResult.Coalesced
        val existing = pending.indexOfFirst { it.notificationKey == request.notificationKey }
        if (existing >= 0) {
            pending[existing] = request
            return OfferResult.Coalesced
        }
        val dropped = if (pending.size >= maxPending) pending.removeFirst() else null
        pending.addLast(request)
        return if (dropped == null) OfferResult.Added else OfferResult.DroppedOldest(dropped.notificationKey)
    }

    fun takeNext(): RewriteRequest? {
        if (activeKey != null) return null
        val next = pending.removeFirstOrNull() ?: return null
        activeKey = next.notificationKey
        return next
    }

    fun complete(notificationKey: String) {
        if (activeKey == notificationKey) activeKey = null
    }

    fun cancel(notificationKey: String): Boolean {
        val wasActive = activeKey == notificationKey
        if (wasActive) activeKey = null
        pending.removeAll { it.notificationKey == notificationKey }
        return wasActive
    }

    fun clear() {
        activeKey = null
        pending.clear()
    }

    fun pendingKeys(): List<String> = pending.map { it.notificationKey }
}

sealed interface OfferResult {
    data object Added : OfferResult
    data object Coalesced : OfferResult
    data class DroppedOldest(val notificationKey: String) : OfferResult
}
