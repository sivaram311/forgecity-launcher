package buzz.delena.forgecity.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Explicit schema upgrades. Prefer these over destructive fallback so
 * scrap/power/focus/goldDust and quest progress survive version bumps.
 */
object ForgeCityMigrations {
    /** v1 → v2: harvest debounce column + durable building levels. */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE city_meta ADD COLUMN lastHarvestEpoch INTEGER NOT NULL DEFAULT 0",
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS building_stats (
                  id TEXT NOT NULL PRIMARY KEY,
                  launchCount INTEGER NOT NULL,
                  level INTEGER NOT NULL
                )
                """.trimIndent(),
            )
        }
    }
}
