package buzz.delena.forgecity.city

enum class District(
    val displayName: String,
    val unlockChapter: Int,
) {
    FORGE("Forge District", 1),
    VAULT("Vault Tower", 2),
    NEXUS("Nexus Plaza", 2),
    ARENA("Arena", 3),
    GARDEN("Garden", 3),
    ARCHIVE("Archive", 2),
    CUSTOM("Custom District", 5),
}
