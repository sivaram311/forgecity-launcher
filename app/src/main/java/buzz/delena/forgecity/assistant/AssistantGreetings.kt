package buzz.delena.forgecity.assistant

import kotlin.random.Random

/**
 * Pure content/logic for Assistant home-mode greetings and streaks. No
 * Android dependencies so this is plain-JVM unit-testable.
 */
object AssistantGreetings {

    private val morning = listOf(
        "Morning! Ready when you are.",
        "Good morning. Let's make today count.",
    )
    private val afternoon = listOf(
        "Hey, welcome back.",
        "Good afternoon! What's next?",
    )
    private val evening = listOf(
        "Evening. Good to see you.",
        "Hey there, winding down for the day?",
    )
    private val night = listOf(
        "Still up? I'm here if you need me.",
        "Quiet night. Welcome back.",
    )

    private val tapReactions = listOf(
        "Hey, that tickles.",
        "Yes? I'm listening.",
        "Hi!",
        "You called?",
    )

    /** Device hour 0-23 → a time-of-day greeting line. */
    fun greetingForHour(hour: Int, random: Random = Random.Default): String {
        val pool = when (hour) {
            in 5..11 -> morning
            in 12..16 -> afternoon
            in 17..20 -> evening
            else -> night
        }
        return pool[random.nextInt(pool.size)]
    }

    fun tapReaction(random: Random = Random.Default): String =
        tapReactions[random.nextInt(tapReactions.size)]

    /** Escalating callout once a streak is worth mentioning; null otherwise. */
    fun streakSuffix(streakDays: Int): String? = when {
        streakDays >= 30 -> " Thirty days running now."
        streakDays >= 7 -> " A full week in a row!"
        streakDays >= 3 -> " Three days in a row now."
        else -> null
    }

    /**
     * Same day as [lastEpochDay] is a no-op (returns [currentStreak], floored
     * at 1); the next consecutive day increments; any larger gap (or first
     * ever run, [lastEpochDay] == -1) resets to 1.
     */
    fun computeStreak(lastEpochDay: Long, currentStreak: Int, todayEpochDay: Long): Int =
        when (todayEpochDay - lastEpochDay) {
            0L -> currentStreak.coerceAtLeast(1)
            1L -> currentStreak + 1
            else -> 1
        }
}
