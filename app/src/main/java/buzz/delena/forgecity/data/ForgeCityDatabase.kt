package buzz.delena.forgecity.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CityMetaEntity::class,
        BuildingEntity::class,
        BuildingStatEntity::class,
        StoryProgressEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class ForgeCityDatabase : RoomDatabase() {
    abstract fun cityDao(): CityDao

    companion object {
        @Volatile
        private var instance: ForgeCityDatabase? = null

        fun get(context: Context): ForgeCityDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    ForgeCityDatabase::class.java,
                    "forgecity.db",
                )
                    .addMigrations(ForgeCityMigrations.MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }
    }
}
