package buzz.delena.forgecity.house

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HouseFeatureFlagsTest {
    @Test
    fun wave1EnablesHouseSurfaceAndKeepsVideoOff() {
        assertTrue(HouseFeatureFlags.use3dHouse)
        assertTrue(HouseFeatureFlags.useFilamentHouse)
        assertFalse(HouseFeatureFlags.useCityVideo)
    }
}
