package buzz.delena.forgecity.house

import buzz.delena.forgecity.city.District

/**
 * Maps city [District] values onto house rooms (Grok roles adapted to repo enum).
 *
 * - Kitchen ← ARCHIVE (utility / files)
 * - Living ← NEXUS (social); ARENA also uses Living (TV / entertainment)
 * - Office ← FORGE (productivity)
 * - Workshop ← CUSTOM (dev / forge overflow workspace)
 * - Bedroom ← GARDEN (personal)
 * - Vault ← VAULT (finance)
 * - Hallway is transit-only (no primary district); used as placement overflow
 */
object DistrictRoomMap {
    fun roomFor(district: District): HouseRoom = when (district) {
        District.FORGE -> HouseRoom.OFFICE
        District.VAULT -> HouseRoom.VAULT
        District.NEXUS -> HouseRoom.LIVING
        District.ARENA -> HouseRoom.LIVING
        District.GARDEN -> HouseRoom.BEDROOM
        District.ARCHIVE -> HouseRoom.KITCHEN
        District.CUSTOM -> HouseRoom.WORKSHOP
    }

    /** Inverse lookup: districts that primarily inhabit [room] (Hallway is empty). */
    fun districtsIn(room: HouseRoom): List<District> =
        District.entries.filter { roomFor(it) == room }
}
