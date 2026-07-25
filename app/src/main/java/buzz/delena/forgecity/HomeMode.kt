package buzz.delena.forgecity

enum class HomeMode {
    CITY,
    HOUSE,
    ASSISTANT,
    PRODUCTION_HOUSE;

    fun next(): HomeMode = entries[(ordinal + 1) % entries.size]

    companion object {
        fun fromPersisted(value: String?): HomeMode? =
            entries.firstOrNull { it.name == value }
    }
}
