package buzz.delena.forgecity.house

import buzz.delena.forgecity.city.District

/**
 * Minimal app identity for placement. Pure Kotlin — ViewModel can map
 * [buzz.delena.forgecity.city.CityBuilding] + launch counts into this.
 */
data class PlaceableApp(
    val id: String,
    val district: District,
    val isFavorite: Boolean = false,
    val launchCount: Int = 0,
)

/** Result of assigning one app to one hotspot. */
data class AppPlacement(
    val appId: String,
    val hotspot: AppHotspot,
)

/**
 * Deterministic app → hotspot assignment.
 *
 * Rules:
 * 1. Sort apps: favorites first, then launchCount desc, then id asc.
 * 2. Prefer the district's home room ([DistrictRoomMap]); overflow to Hallway,
 *    then other rooms by [HouseRoom] ordinal.
 * 3. Favorites prefer DESK then TV then SHELF; others prefer SHELF then DESK then TV.
 * 4. Cap occupied slots per room at [maxAppsPerRoom] (DOOR never used for apps).
 * 5. Same inputs always yield the same placements (stable IDs).
 */
object AppPlacementEngine {
    fun place(
        apps: List<PlaceableApp>,
        hotspots: List<AppHotspot> = HouseHotspots.all,
        maxAppsPerRoom: Int = HouseHotspots.MAX_APPS_PER_ROOM,
    ): List<AppPlacement> {
        require(maxAppsPerRoom >= 0)

        val placeableByRoom = hotspots
            .filter { it.kind != HotspotKind.DOOR }
            .groupBy { it.room }
            .mapValues { (_, spots) -> spots.sortedBy { it.id } }

        val occupied = mutableSetOf<String>()
        val roomCounts = mutableMapOf<HouseRoom, Int>()
        val placements = mutableListOf<AppPlacement>()

        val ordered = apps.sortedWith(
            compareByDescending<PlaceableApp> { it.isFavorite }
                .thenByDescending { it.launchCount }
                .thenBy { it.id },
        )

        for (app in ordered) {
            val home = DistrictRoomMap.roomFor(app.district)
            val roomOrder = roomSearchOrder(home)
            val kindOrder = kindPreference(favorite = app.isFavorite)

            val chosen = roomOrder.firstNotNullOfOrNull { room ->
                if ((roomCounts[room] ?: 0) >= maxAppsPerRoom) return@firstNotNullOfOrNull null
                val candidates = placeableByRoom[room].orEmpty()
                    .filter { it.id !in occupied }
                    .sortedWith(
                        compareBy<AppHotspot> { kindOrder.indexOf(it.kind).let { i -> if (i < 0) Int.MAX_VALUE else i } }
                            .thenBy { it.id },
                    )
                candidates.firstOrNull()
            } ?: continue

            occupied += chosen.id
            roomCounts[chosen.room] = (roomCounts[chosen.room] ?: 0) + 1
            placements += AppPlacement(appId = app.id, hotspot = chosen)
        }

        return placements
    }

    /**
     * ViewModel-friendly entry: buildings + launch counts keyed by building id
     * (typically [CityBuilding.id] / component flatten string).
     */
    fun placeBuildings(
        buildingIds: List<String>,
        districtOf: (String) -> District,
        isFavorite: (String) -> Boolean,
        launchCountOf: (String) -> Int,
        hotspots: List<AppHotspot> = HouseHotspots.all,
        maxAppsPerRoom: Int = HouseHotspots.MAX_APPS_PER_ROOM,
    ): List<AppPlacement> {
        val apps = buildingIds.map { id ->
            PlaceableApp(
                id = id,
                district = districtOf(id),
                isFavorite = isFavorite(id),
                launchCount = launchCountOf(id),
            )
        }
        return place(apps, hotspots, maxAppsPerRoom)
    }

    private fun kindPreference(favorite: Boolean): List<HotspotKind> =
        if (favorite) {
            listOf(HotspotKind.DESK, HotspotKind.TV, HotspotKind.SHELF)
        } else {
            listOf(HotspotKind.SHELF, HotspotKind.DESK, HotspotKind.TV)
        }

    private fun roomSearchOrder(home: HouseRoom): List<HouseRoom> = buildList {
        add(home)
        if (home != HouseRoom.HALLWAY) add(HouseRoom.HALLWAY)
        HouseRoom.entries.forEach { room ->
            if (room != home && room != HouseRoom.HALLWAY) add(room)
        }
    }
}
