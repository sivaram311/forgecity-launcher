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

    @Query("SELECT * FROM city_meta WHERE id = 1")
    suspend fun getMeta(): CityMetaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMeta(meta: CityMetaEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMetaIfAbsent(meta: CityMetaEntity)

    @Query("SELECT * FROM buildings")
    fun observeBuildings(): Flow<List<BuildingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBuildings(buildings: List<BuildingEntity>)

    @Query("DELETE FROM buildings")
    suspend fun clearBuildings()

    @Query("SELECT * FROM building_stats")
    suspend fun getBuildingStats(): List<BuildingStatEntity>

    @Query("SELECT * FROM building_stats WHERE id = :id")
    suspend fun getBuildingStat(id: String): BuildingStatEntity?

    @Query("SELECT * FROM building_stats WHERE isFavorite = 1")
    suspend fun getFavorites(): List<BuildingStatEntity>

    @Query("SELECT COUNT(*) FROM building_stats WHERE isFavorite = 1")
    suspend fun favoriteCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBuildingStat(stat: BuildingStatEntity)

    @Query("UPDATE building_stats SET isFavorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: String, favorite: Boolean)

    @Query("SELECT * FROM story_progress")
    fun observeQuests(): Flow<List<StoryProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertQuest(quest: StoryProgressEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertQuestIfAbsent(quest: StoryProgressEntity)

    @Query(
        """
        UPDATE city_meta SET
          scrap = scrap + :scrap,
          power = power + :power,
          focus = focus + :focus,
          goldDust = goldDust + :goldDust
        WHERE id = 1
        """,
    )
    suspend fun addResources(scrap: Int, power: Int, focus: Int, goldDust: Int)

    @Query("UPDATE city_meta SET lastHarvestEpoch = :epoch WHERE id = 1")
    suspend fun setLastHarvest(epoch: Long)
}
