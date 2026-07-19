package buzz.delena.forgecity.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "city_meta")
data class CityMetaEntity(
    @PrimaryKey val id: Int = 1,
    val chapterId: Int = 1,
    val chapterTitle: String = "Embers",
    val scrap: Int = 20,
    val power: Int = 0,
    val focus: Int = 0,
    val goldDust: Int = 0,
    val lastHarvestEpoch: Long = 0,
)

@Entity(tableName = "buildings")
data class BuildingEntity(
    @PrimaryKey val id: String,
    val packageName: String,
    val activityName: String,
    val label: String,
    val district: String,
    val col: Int,
    val row: Int,
    val level: Int = 1,
)

/** Durable per-building progression (Stream C SoT for levels). */
@Entity(tableName = "building_stats")
data class BuildingStatEntity(
    @PrimaryKey val id: String,
    val launchCount: Int = 0,
    val level: Int = 1,
)

@Entity(tableName = "story_progress")
data class StoryProgressEntity(
    @PrimaryKey val questId: String,
    val chapterId: Int,
    val title: String,
    val status: String,
    val progress: Int = 0,
    val goal: Int = 1,
)
