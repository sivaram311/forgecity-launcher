package buzz.delena.forgecity.house

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Minimal device smoke for house HOME. Ignored so CI unit tests / hosts without a
 * Realme UI session never fail. Un-ignore on a physical device when running #16.
 *
 * See `docs/DEVICE-E2E-HOUSE-CHECKLIST.md`.
 */
@RunWith(AndroidJUnit4::class)
@Ignore("Requires physical device / house HOME UI; see DEVICE-E2E-HOUSE-CHECKLIST.md")
class HouseInstrumentedSmokeTest {

    @Test
    fun packageName_matchesForgeCity() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("buzz.delena.forgecity", context.packageName)
    }
}
