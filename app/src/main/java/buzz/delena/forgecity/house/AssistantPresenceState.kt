package buzz.delena.forgecity.house

/**
 * Pure, unit-testable speaking window for the house assistant character.
 * [isSpeaking] is true until [speakUntilMs] (exclusive) relative to [clock].
 */
class AssistantPresenceState(
    private val clock: () -> Long = System::currentTimeMillis,
) {
    @Volatile
    private var speakUntilMs: Long = 0L

    /** Marks speaking for [durationMs] from now (extends if already speaking). */
    fun markSpeaking(durationMs: Long = DEFAULT_SPEAK_MS) {
        val now = clock()
        val until = now + durationMs.coerceAtLeast(0L)
        speakUntilMs = maxOf(speakUntilMs, until)
    }

    fun clear() {
        speakUntilMs = 0L
    }

    fun isSpeaking(nowMs: Long = clock()): Boolean = nowMs < speakUntilMs

    fun remainingMs(nowMs: Long = clock()): Long = (speakUntilMs - nowMs).coerceAtLeast(0L)

    companion object {
        const val DEFAULT_SPEAK_MS: Long = 2_500L
    }
}
