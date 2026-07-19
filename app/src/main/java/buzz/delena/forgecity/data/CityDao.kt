package buzz.delena.forgecity.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CityDao {
    @Query("SELECT * FROM city_meta WHERE id = 1")
    fun observeMeta(): Flow<CityMetaEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMeta(meta: CityMetaEntity)

    @Query("SELECT * FROM buildings")
    fun observeBuildings(): Flow<List<BuildingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBuildings(buildings: List<BuildingEntity>)

    @Query("DELETE FROM buildings")
    suspend fun clearBuildings()

    @Query("SELECT * FROM story_progress")
    fun observeQuests(): Flow<List<StoryProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertQuest(quest: StoryProgressEntity)
}
