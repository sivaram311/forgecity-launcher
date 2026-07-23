package buzz.delena.forgecity.house

import buzz.delena.forgecity.city.District
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DistrictRoomMapTest {
    @Test
    fun mapsDistrictsToGrokRoles() {
        assertEquals(HouseRoom.OFFICE, DistrictRoomMap.roomFor(District.FORGE))
        assertEquals(HouseRoom.VAULT, DistrictRoomMap.roomFor(District.VAULT))
        assertEquals(HouseRoom.LIVING, DistrictRoomMap.roomFor(District.NEXUS))
        assertEquals(HouseRoom.LIVING, DistrictRoomMap.roomFor(District.ARENA))
        assertEquals(HouseRoom.BEDROOM, DistrictRoomMap.roomFor(District.GARDEN))
        assertEquals(HouseRoom.KITCHEN, DistrictRoomMap.roomFor(District.ARCHIVE))
        assertEquals(HouseRoom.WORKSHOP, DistrictRoomMap.roomFor(District.CUSTOM))
    }

    @Test
    fun everyDistrictHasARoom() {
        District.entries.forEach { district ->
            DistrictRoomMap.roomFor(district)
        }
    }

    @Test
    fun hallwayHasNoPrimaryDistrict() {
        assertTrue(DistrictRoomMap.districtsIn(HouseRoom.HALLWAY).isEmpty())
    }
}

class HouseRoomBoundsTest {
    @Test
    fun kitchenMatchesGrokAabb() {
        val b = HouseRoom.KITCHEN.bounds
        assertEquals(0f, b.minX, 0.001f)
        assertEquals(0f, b.minZ, 0.001f)
        assertEquals(3f, b.maxX, 0.001f)
        assertEquals(3f, b.maxZ, 0.001f)
        assertEquals(2.5f, b.height, 0.001f)
    }

    @Test
    fun livingAndOfficeMatchGrokAabb() {
        val living = HouseRoom.LIVING.bounds
        assertEquals(3f, living.minX, 0.001f)
        assertEquals(0f, living.minZ, 0.001f)
        assertEquals(7f, living.maxX, 0.001f)
        assertEquals(3f, living.maxZ, 0.001f)

        val office = HouseRoom.OFFICE.bounds
        assertEquals(3f, office.minX, 0.001f)
        assertEquals(3f, office.minZ, 0.001f)
        assertEquals(7f, office.maxX, 0.001f)
        assertEquals(6f, office.maxZ, 0.001f)
        assertEquals(2.5f, office.height, 0.001f)
    }

    @Test
    fun boundsContainInteriorPoint() {
        assertTrue(HouseRoom.KITCHEN.bounds.contains(1.5f, 1.5f))
        assertFalse(HouseRoom.KITCHEN.bounds.contains(4f, 1f))
    }
}

class AppPlacementEngineTest {
    @Test
    fun placesAppInDistrictHomeRoom() {
        val apps = listOf(
            PlaceableApp(id = "forge.app", district = District.FORGE, launchCount = 3),
            PlaceableApp(id = "nexus.app", district = District.NEXUS, launchCount = 1),
            PlaceableApp(id = "vault.app", district = District.VAULT, launchCount = 2),
        )
        val placements = AppPlacementEngine.place(apps)
        assertEquals(3, placements.size)
        assertEquals(HouseRoom.OFFICE, placements.single { it.appId == "forge.app" }.hotspot.room)
        assertEquals(HouseRoom.LIVING, placements.single { it.appId == "nexus.app" }.hotspot.room)
        assertEquals(HouseRoom.VAULT, placements.single { it.appId == "vault.app" }.hotspot.room)
    }

    @Test
    fun favoritesPreferDeskOrTv() {
        val apps = listOf(
            PlaceableApp(id = "fav", district = District.NEXUS, isFavorite = true, launchCount = 1),
            PlaceableApp(id = "normal", district = District.NEXUS, isFavorite = false, launchCount = 99),
        )
        val placements = AppPlacementEngine.place(apps)
        val favKind = placements.single { it.appId == "fav" }.hotspot.kind
        assertTrue(favKind == HotspotKind.DESK || favKind == HotspotKind.TV)
        assertEquals(HotspotKind.SHELF, placements.single { it.appId == "normal" }.hotspot.kind)
    }

    @Test
    fun placementIsDeterministicAndStable() {
        val apps = listOf(
            PlaceableApp("c", District.FORGE, isFavorite = false, launchCount = 5),
            PlaceableApp("a", District.FORGE, isFavorite = true, launchCount = 1),
            PlaceableApp("b", District.NEXUS, isFavorite = false, launchCount = 10),
            PlaceableApp("d", District.ARCHIVE, isFavorite = true, launchCount = 2),
        )
        val first = AppPlacementEngine.place(apps)
        val second = AppPlacementEngine.place(apps.shuffled())
        assertEquals(first.map { it.appId to it.hotspot.id }, second.map { it.appId to it.hotspot.id })
    }

    @Test
    fun respectsPerRoomCapAndOverflowsToHallway() {
        val apps = (1..8).map { i ->
            PlaceableApp(id = "forge-$i", district = District.FORGE, launchCount = i)
        }
        val placements = AppPlacementEngine.place(apps, maxAppsPerRoom = 4)
        assertEquals(8, placements.size)
        val officeCount = placements.count { it.hotspot.room == HouseRoom.OFFICE }
        assertEquals(4, officeCount)
        assertTrue(placements.any { it.hotspot.room == HouseRoom.HALLWAY })
    }

    @Test
    fun neverAssignsDoorHotspots() {
        val apps = District.entries.mapIndexed { index, district ->
            PlaceableApp(id = "app-$index", district = district, launchCount = index)
        }
        val placements = AppPlacementEngine.place(apps)
        assertTrue(placements.none { it.hotspot.kind == HotspotKind.DOOR })
    }

    @Test
    fun placeBuildingsMatchesPlaceApi() {
        val ids = listOf("x", "y")
        val districts = mapOf("x" to District.GARDEN, "y" to District.CUSTOM)
        val viaPlace = AppPlacementEngine.place(
            listOf(
                PlaceableApp("x", District.GARDEN, launchCount = 4),
                PlaceableApp("y", District.CUSTOM, isFavorite = true, launchCount = 1),
            ),
        )
        val viaBuildings = AppPlacementEngine.placeBuildings(
            buildingIds = ids,
            districtOf = { districts.getValue(it) },
            isFavorite = { it == "y" },
            launchCountOf = { if (it == "x") 4 else 1 },
        )
        assertEquals(
            viaPlace.map { it.appId to it.hotspot.id },
            viaBuildings.map { it.appId to it.hotspot.id },
        )
    }

    @Test
    fun hotspotIdsAreUnique() {
        val ids = HouseHotspots.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }
}
