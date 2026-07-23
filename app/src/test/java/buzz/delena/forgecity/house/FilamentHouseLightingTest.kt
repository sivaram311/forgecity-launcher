package buzz.delena.forgecity.house

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FilamentHouseLightingTest {
    @Test
    fun dayIsBrighterSunThanNight() {
        assertTrue(FilamentHouseLighting.day.sunIntensity > FilamentHouseLighting.night.sunIntensity)
    }

    @Test
    fun forNightSelectsModes() {
        assertEquals(FilamentHouseLighting.night, FilamentHouseLighting.forNight(true))
        assertEquals(FilamentHouseLighting.day, FilamentHouseLighting.forNight(false))
    }

    @Test
    fun exposureUsesPhotographicStopsNotTinyEv() {
        // Bare EV ~1.x caused solid white; aperture must stay in real f-stop range.
        assertTrue(FilamentHouseLighting.day.aperture >= 4f)
        assertTrue(FilamentHouseLighting.day.aperture <= 22f)
        assertTrue(FilamentHouseLighting.day.iso in 50f..1600f)
        assertTrue(FilamentHouseLighting.day.sunIntensity <= 8_000f)
        assertTrue(FilamentHouseLighting.day.iblIntensity <= 5_000f)
        assertTrue(!FilamentHouseLighting.day.bloomEnabled)
    }

    @Test
    fun speechPulseAboveOne() {
        assertTrue(FilamentHouseLighting.day.speechPulseScale > 1f)
    }
}

class HouseWorldTest {
    @Test
    fun roomLookup() {
        assertEquals(HouseRoom.VAULT, HouseWorld.roomById("vault"))
        assertEquals(HouseRoom.HALLWAY, HouseWorld.roomById("hall"))
    }

    @Test
    fun positionInRoomCenter() {
        val (x, y, z) = HouseWorld.positionInRoom(HouseRoom.KITCHEN, 0.5f, 0.5f)
        assertEquals(1.5f, x, 0.01f)
        assertEquals(HouseWorld.FLOOR_Y, y, 0.001f)
        assertEquals(1.5f, z, 0.01f)
    }
}
