package buzz.delena.forgecity.ui.lot

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductionHouseUrlTest {
    @Test
    fun productionHouseUrlIsHttpsProdHost() {
        assertEquals("https://production-house.delena.buzz", PRODUCTION_HOUSE_URL)
        assertTrue(PRODUCTION_HOUSE_URL.startsWith("https://"))
    }
}
