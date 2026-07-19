package buzz.delena.forgecity.city

data class CityResources(
    val scrap: Int = 0,
    val power: Int = 0,
    val focus: Int = 0,
    val goldDust: Int = 0,
)

data class CityState(
    val chapterId: Int = 1,
    val chapterTitle: String = "Embers",
    val briefing: String = "The city is mostly dark. Organize apps into starter buildings.",
    val resources: CityResources = CityResources(),
    val unlockedDistricts: Set<District> = setOf(District.FORGE),
    val buildings: List<CityBuilding> = emptyList(),
)
