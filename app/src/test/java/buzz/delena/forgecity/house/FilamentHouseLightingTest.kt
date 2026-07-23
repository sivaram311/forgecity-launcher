package buzz.delena.forgecity.house

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FilamentHouseLightingTest {
    @Test
    fun dayIsBrighterThanNight() {
        assertTrue(FilamentHouseLighting.day.sunIntensity > FilamentHouseLighting.night.sunIntensity)
    }

    @Test
    fun forNightSelectsModes() {
        assertEquals(FilamentHouseLighting.night, FilamentHouseLighting.forNight(true))
        assertEquals(FilamentHouseLighting.day, FilamentHouseLighting.forNight(false))
    }

    @Test
    fun speechPulseAboveOne() {
        assertTrue(FilamentHouseLighting.day.speechPulseScale > 1f)
        assertTrue(FilamentHouseLighting.night.speechPulseScale > 1f)
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
