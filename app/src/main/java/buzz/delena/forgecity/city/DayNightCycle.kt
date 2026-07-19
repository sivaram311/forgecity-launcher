package buzz.delena.forgecity.city

/**
 * Real-clock day/night palette for sky + night glows.
 * Pure functions — unit-testable without Android.
 */
object DayNightCycle {
    fun isNight(hourOfDay: Int): Boolean = hourOfDay < 6 || hourOfDay >= 19

    fun skyColors(hourOfDay: Int): Triple<Long, Long, Long> = when (hourOfDay) {
        in 0..4 -> Triple(0xFF0A0C18, 0xFF05060C, 0xFF10101A)
        in 5..7 -> Triple(0xFF3A2A4A, 0xFF1B1624, 0xFF6B3A2A)
        in 8..11 -> Triple(0xFF4A6FA5, 0xFF2A4060, 0xFF8AA8C8)
        in 12..16 -> Triple(0xFF3D6EA8, 0xFF1E3A5C, 0xFF7AA0C8)
        in 17..18 -> Triple(0xFF6B3A5A, 0xFF2A1828, 0xFFC87840)
        else -> Triple(0xFF0E1020, 0xFF060810, 0xFF181828)
    }

    fun starAlpha(hourOfDay: Int): Float = when {
        hourOfDay in 0..5 || hourOfDay >= 20 -> 0.85f
        hourOfDay == 6 || hourOfDay == 19 -> 0.35f
        else -> 0f
    }
}
