package buzz.delena.forgecity.city

object DistrictClassifier {
    private val forgeHints = listOf(
        "code", "studio", "note", "calendar", "docs", "notion", "obsidian", "term", "git", "slack",
        "office", "word", "excel", "drive", "keep", "todo", "task", "jira", "figma",
    )
    private val vaultHints = listOf(
        "bank", "pay", "wallet", "trade", "invest", "stock", "crypto", "mt5", "forex", "gold",
        "finance", "money", "upi", "gpay", "phonepe",
    )
    private val nexusHints = listOf(
        "whatsapp", "telegram", "signal", "message", "sms", "mail", "gmail", "phone", "dial",
        "contact", "discord", "instagram", "twitter", "x.com", "facebook", "linkedin",
    )
    private val arenaHints = listOf(
        "youtube", "game", "play", "netflix", "spotify", "music", "twitch", "reel", "tiktok",
        "prime", "hotstar", "steam",
    )
    private val gardenHints = listOf(
        "health", "fit", "yoga", "mind", "meditat", "sleep", "habit", "calorie", "run", "step",
    )
    private val archiveHints = listOf(
        "file", "gallery", "photo", "download", "drive", "dropbox", "cloud", "scanner", "pdf",
    )

    fun classify(packageName: String, label: String): District {
        val haystack = "$packageName $label".lowercase()
        return when {
            matches(haystack, forgeHints) -> District.FORGE
            matches(haystack, vaultHints) -> District.VAULT
            matches(haystack, nexusHints) -> District.NEXUS
            matches(haystack, arenaHints) -> District.ARENA
            matches(haystack, gardenHints) -> District.GARDEN
            matches(haystack, archiveHints) -> District.ARCHIVE
            else -> District.FORGE
        }
    }

    private fun matches(haystack: String, hints: List<String>): Boolean =
        hints.any { haystack.contains(it) }
}
