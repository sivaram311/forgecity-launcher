package buzz.delena.forgecity.house

/**
 * Interior rooms for the 3D house HOME. Meter AABB on the floor plane (X/Z),
 * origin at the SW corner of the house shell (Grok Wave-1 layout).
 */
enum class HouseRoom(
    val displayName: String,
    val role: String,
    val bounds: RoomBounds,
) {
    KITCHEN(
        displayName = "Kitchen",
        role = "Utility",
        bounds = RoomBounds(minX = 0f, minZ = 0f, maxX = 3f, maxZ = 3f, height = 2.5f),
    ),
    LIVING(
        displayName = "Living",
        role = "Social",
        bounds = RoomBounds(minX = 3f, minZ = 0f, maxX = 7f, maxZ = 3f, height = 2.5f),
    ),
    HALLWAY(
        displayName = "Hallway",
        role = "Transit",
        bounds = RoomBounds(minX = 0f, minZ = 3f, maxX = 3f, maxZ = 6f, height = 2.5f),
    ),
    OFFICE(
        displayName = "Office",
        role = "Productivity",
        bounds = RoomBounds(minX = 3f, minZ = 3f, maxX = 7f, maxZ = 6f, height = 2.5f),
    ),
    BEDROOM(
        displayName = "Bedroom",
        role = "Personal",
        bounds = RoomBounds(minX = 0f, minZ = 6f, maxX = 3f, maxZ = 9f, height = 2.5f),
    ),
    WORKSHOP(
        displayName = "Workshop",
        role = "Dev",
        bounds = RoomBounds(minX = 3f, minZ = 6f, maxX = 7f, maxZ = 9f, height = 2.5f),
    ),
    VAULT(
        displayName = "Vault",
        role = "Finance",
        bounds = RoomBounds(minX = 7f, minZ = 3f, maxX = 9f, maxZ = 6f, height = 2.5f),
    ),
}

/** Axis-aligned floor footprint in meters plus ceiling height. */
data class RoomBounds(
    val minX: Float,
    val minZ: Float,
    val maxX: Float,
    val maxZ: Float,
    val height: Float,
) {
    val width: Float get() = maxX - minX
    val depth: Float get() = maxZ - minZ

    fun contains(x: Float, z: Float): Boolean =
        x in minX..maxX && z in minZ..maxZ
}
