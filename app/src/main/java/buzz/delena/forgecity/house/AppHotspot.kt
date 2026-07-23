package buzz.delena.forgecity.house

/** Furniture / portal slot an installed app can occupy inside a room. */
enum class HotspotKind {
    SHELF,
    DESK,
    TV,
    DOOR,
}

/**
 * Stable hotspot definition. [localOffset] is meters from the room SW corner
 * (relative to [HouseRoom.bounds] minX/minZ), Y up.
 */
data class AppHotspot(
    val id: String,
    val room: HouseRoom,
    val localOffset: Vec3,
    val kind: HotspotKind,
)

data class Vec3(
    val x: Float,
    val y: Float,
    val z: Float,
)

/**
 * Default Wave-1 hotspot catalog. IDs are stable across builds so placement
 * remains deterministic when the app list is unchanged.
 */
object HouseHotspots {
    /** Soft cap on occupied app slots per room (DOOR slots never count). */
    const val MAX_APPS_PER_ROOM = 6

    val all: List<AppHotspot> = listOf(
        // Kitchen
        spot(HouseRoom.KITCHEN, "shelf", 1, 0.4f, 1.0f, 0.5f, HotspotKind.SHELF),
        spot(HouseRoom.KITCHEN, "shelf", 2, 1.5f, 1.0f, 0.5f, HotspotKind.SHELF),
        spot(HouseRoom.KITCHEN, "shelf", 3, 2.5f, 1.0f, 0.5f, HotspotKind.SHELF),
        spot(HouseRoom.KITCHEN, "desk", 1, 1.5f, 0.75f, 2.2f, HotspotKind.DESK),
        spot(HouseRoom.KITCHEN, "door", 1, 2.8f, 0f, 2.8f, HotspotKind.DOOR),
        // Living
        spot(HouseRoom.LIVING, "shelf", 1, 0.5f, 1.0f, 0.4f, HotspotKind.SHELF),
        spot(HouseRoom.LIVING, "shelf", 2, 3.2f, 1.0f, 0.4f, HotspotKind.SHELF),
        spot(HouseRoom.LIVING, "desk", 1, 1.5f, 0.75f, 1.5f, HotspotKind.DESK),
        spot(HouseRoom.LIVING, "tv", 1, 2.0f, 1.2f, 0.3f, HotspotKind.TV),
        spot(HouseRoom.LIVING, "tv", 2, 3.0f, 1.2f, 0.3f, HotspotKind.TV),
        spot(HouseRoom.LIVING, "door", 1, 0.2f, 0f, 1.5f, HotspotKind.DOOR),
        // Hallway (transit + overflow shelves)
        spot(HouseRoom.HALLWAY, "shelf", 1, 1.2f, 1.0f, 1.0f, HotspotKind.SHELF),
        spot(HouseRoom.HALLWAY, "shelf", 2, 1.2f, 1.0f, 2.0f, HotspotKind.SHELF),
        spot(HouseRoom.HALLWAY, "door", 1, 1.5f, 0f, 0.1f, HotspotKind.DOOR),
        spot(HouseRoom.HALLWAY, "door", 2, 1.5f, 0f, 2.9f, HotspotKind.DOOR),
        spot(HouseRoom.HALLWAY, "door", 3, 2.9f, 0f, 1.5f, HotspotKind.DOOR),
        // Office
        spot(HouseRoom.OFFICE, "shelf", 1, 0.4f, 1.0f, 0.5f, HotspotKind.SHELF),
        spot(HouseRoom.OFFICE, "shelf", 2, 0.4f, 1.0f, 2.5f, HotspotKind.SHELF),
        spot(HouseRoom.OFFICE, "desk", 1, 2.0f, 0.75f, 1.5f, HotspotKind.DESK),
        spot(HouseRoom.OFFICE, "desk", 2, 3.2f, 0.75f, 1.5f, HotspotKind.DESK),
        spot(HouseRoom.OFFICE, "tv", 1, 2.0f, 1.2f, 0.3f, HotspotKind.TV),
        spot(HouseRoom.OFFICE, "door", 1, 0.2f, 0f, 1.5f, HotspotKind.DOOR),
        // Bedroom
        spot(HouseRoom.BEDROOM, "shelf", 1, 0.5f, 1.0f, 0.5f, HotspotKind.SHELF),
        spot(HouseRoom.BEDROOM, "shelf", 2, 2.4f, 1.0f, 0.5f, HotspotKind.SHELF),
        spot(HouseRoom.BEDROOM, "desk", 1, 1.5f, 0.75f, 2.2f, HotspotKind.DESK),
        spot(HouseRoom.BEDROOM, "door", 1, 2.8f, 0f, 1.5f, HotspotKind.DOOR),
        // Workshop
        spot(HouseRoom.WORKSHOP, "shelf", 1, 0.4f, 1.0f, 0.5f, HotspotKind.SHELF),
        spot(HouseRoom.WORKSHOP, "shelf", 2, 0.4f, 1.0f, 2.5f, HotspotKind.SHELF),
        spot(HouseRoom.WORKSHOP, "shelf", 3, 3.5f, 1.0f, 1.5f, HotspotKind.SHELF),
        spot(HouseRoom.WORKSHOP, "desk", 1, 2.0f, 0.75f, 2.0f, HotspotKind.DESK),
        spot(HouseRoom.WORKSHOP, "desk", 2, 3.2f, 0.75f, 2.0f, HotspotKind.DESK),
        spot(HouseRoom.WORKSHOP, "door", 1, 0.2f, 0f, 1.5f, HotspotKind.DOOR),
        // Vault
        spot(HouseRoom.VAULT, "shelf", 1, 0.4f, 1.0f, 0.5f, HotspotKind.SHELF),
        spot(HouseRoom.VAULT, "shelf", 2, 0.4f, 1.0f, 1.5f, HotspotKind.SHELF),
        spot(HouseRoom.VAULT, "desk", 1, 1.0f, 0.75f, 1.5f, HotspotKind.DESK),
        spot(HouseRoom.VAULT, "door", 1, 0.1f, 0f, 1.5f, HotspotKind.DOOR),
    )

    fun inRoom(room: HouseRoom): List<AppHotspot> = all.filter { it.room == room }

    /** Non-DOOR hotspots that can hold an app icon. */
    fun placeableInRoom(room: HouseRoom): List<AppHotspot> =
        inRoom(room).filter { it.kind != HotspotKind.DOOR }

    private fun spot(
        room: HouseRoom,
        kindKey: String,
        index: Int,
        x: Float,
        y: Float,
        z: Float,
        kind: HotspotKind,
    ): AppHotspot = AppHotspot(
        id = "${room.name.lowercase()}-$kindKey-$index",
        room = room,
        localOffset = Vec3(x, y, z),
        kind = kind,
    )
}
