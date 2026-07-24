package buzz.delena.forgecity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HomeModeTest {
    @Test
    fun nextCyclesThroughAllThreeModesAndWraps() {
        assertEquals(HomeMode.HOUSE, HomeMode.CITY.next())
        assertEquals(HomeMode.ASSISTANT, HomeMode.HOUSE.next())
        assertEquals(HomeMode.CITY, HomeMode.ASSISTANT.next())
    }

    @Test
    fun fromPersistedRoundTripsAndRejectsUnknown() {
        assertEquals(HomeMode.ASSISTANT, HomeMode.fromPersisted("ASSISTANT"))
        assertNull(HomeMode.fromPersisted("NOT_A_MODE"))
        assertNull(HomeMode.fromPersisted(null))
    }
}
