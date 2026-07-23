package buzz.delena.forgecity.house

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FilamentHouseIblTest {
    @Test
    fun hdrAssetPathIsUnderFilament() {
        assertEquals("filament/house_ibl_256.hdr", FilamentHouseIbl.HDR_ASSET)
    }

    @Test
    fun dayIblCappedForAdreno() {
        assertTrue(FilamentHouseIbl.DAY_IBL in 1_000f..2_200f)
        assertTrue(FilamentHouseIbl.NIGHT_IBL < FilamentHouseIbl.DAY_IBL)
        assertTrue(FilamentHouseIbl.iblIntensity(night = false, dayHemi = 500f) <= 2_000f)
        assertEquals(FilamentHouseIbl.NIGHT_IBL, FilamentHouseIbl.iblIntensity(night = true, dayHemi = null), 0.01f)
    }

    @Test
    fun rimAndReflectanceAreFresnelStandIns() {
        assertTrue(FilamentHouseIbl.RIM_LUX in 800f..1_600f)
        assertTrue(FilamentHouseIbl.SKIN_REFLECTANCE in 0.02f..0.08f)
        assertTrue(FilamentHouseIbl.GLASS_REFLECTANCE > FilamentHouseIbl.CLOTH_REFLECTANCE)
        assertTrue(FilamentHouseIbl.rimIntensity(true, false, 1f) >= FilamentHouseIbl.RIM_LUX * 0.8f)
    }
}
