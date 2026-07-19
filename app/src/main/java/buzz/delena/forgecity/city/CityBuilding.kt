package buzz.delena.forgecity.city

import android.graphics.drawable.Drawable
import android.content.ComponentName

data class CityBuilding(
    val id: String,
    val label: String,
    val packageName: String,
    val component: ComponentName,
    val icon: Drawable?,
    val district: District,
    val col: Int,
    val row: Int,
    val level: Int = 1,
    val isFavorite: Boolean = false,
)
