package buzz.delena.forgecity.house.character

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HouseFaceAssetsTest {
    @Test
    fun sharedFaceIsUnderFacesAssets() {
        assertEquals("faces/siva.png", HouseFaceAssets.SHARED_FACE)
        assertTrue(HouseFaceAssets.SHARED_FACE.endsWith(".png"))
    }
}
