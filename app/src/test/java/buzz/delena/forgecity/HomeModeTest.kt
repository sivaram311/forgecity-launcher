package buzz.delena.forgecity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HomeModeTest {
    @Test
    fun nextCyclesThroughAllFourModesAndWraps() {
        assertEquals(HomeMode.HOUSE, HomeMode.CITY.next())
        assertEquals(HomeMode.ASSISTANT, HomeMode.HOUSE.next())
        assertEquals(HomeMode.PRODUCTION_HOUSE, HomeMode.ASSISTANT.next())
        assertEquals(HomeMode.CITY, HomeMode.PRODUCTION_HOUSE.next())
    }

    @Test
    fun fromPersistedRoundTripsAndRejectsUnknown() {
        assertEquals(HomeMode.ASSISTANT, HomeMode.fromPersisted("ASSISTANT"))
        assertEquals(HomeMode.PRODUCTION_HOUSE, HomeMode.fromPersisted("PRODUCTION_HOUSE"))
        assertNull(HomeMode.fromPersisted("NOT_A_MODE"))
        assertNull(HomeMode.fromPersisted(null))
    }
}
