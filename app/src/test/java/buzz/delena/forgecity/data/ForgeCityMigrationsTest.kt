package buzz.delena.forgecity.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ForgeCityMigrationsTest {
    @Test
    fun migrationOneToTwoSpansExpectedVersions() {
        val migration = ForgeCityMigrations.MIGRATION_1_2
        assertEquals(1, migration.startVersion)
        assertEquals(2, migration.endVersion)
    }

    @Test
    fun migrationObjectIsRegisteredSingleton() {
        assertTrue(ForgeCityMigrations.MIGRATION_1_2 === ForgeCityMigrations.MIGRATION_1_2)
    }
}
