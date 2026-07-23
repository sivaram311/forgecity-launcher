package buzz.delena.forgecity.house

/**
 * Map UI room ids / normalized floor cells onto meter-space positions matching
 * [HouseRoom] AABBs and `assets/filament/house_shell.glb`.
 */
object HouseWorld {
    fun roomById(roomId: String): HouseRoom? = when (roomId.lowercase()) {
        "kitchen" -> HouseRoom.KITCHEN
        "living" -> HouseRoom.LIVING
        "hallway", "hall" -> HouseRoom.HALLWAY
        "office" -> HouseRoom.OFFICE
        "bedroom" -> HouseRoom.BEDROOM
        "workshop" -> HouseRoom.WORKSHOP
        "vault" -> HouseRoom.VAULT
        else -> null
    }

    /** Floor Y for standing characters / markers (meters). */
    const val FLOOR_Y = 0.02f

    fun positionInRoom(room: HouseRoom, nx: Float, nz: Float, y: Float = FLOOR_Y): Triple<Float, Float, Float> {
        val b = room.bounds
        val x = b.minX + nx.coerceIn(0f, 1f) * b.width
        val z = b.minZ + nz.coerceIn(0f, 1f) * b.depth
        return Triple(x, y, z)
    }

    fun hotspotWorld(hotspot: AppHotspot): Triple<Float, Float, Float> {
        val b = hotspot.room.bounds
        return Triple(
            b.minX + hotspot.localOffset.x,
            hotspot.localOffset.y,
            b.minZ + hotspot.localOffset.z,
        )
    }
}
