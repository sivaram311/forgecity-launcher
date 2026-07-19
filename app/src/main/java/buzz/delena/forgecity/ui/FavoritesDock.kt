package buzz.delena.forgecity.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import buzz.delena.forgecity.city.CityBuilding
import buzz.delena.forgecity.city.FavoritePolicy

@Composable
fun FavoritesDock(
    favorites: List<CityBuilding>,
    onFavoriteTap: (CityBuilding) -> Unit,
    modifier: Modifier = Modifier,
) {
    val slots = FavoritePolicy.MAX_FAVORITES
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(78.dp)
            .background(
                brush = Brush.horizontalGradient(
                    listOf(Color(0xCC2A1838), Color(0xCC1A1228), Color(0xCC302018)),
                ),
                shape = RoundedCornerShape(28.dp),
            )
            .border(1.dp, Color(0x66E8A15A), RoundedCornerShape(28.dp))
            .padding(horizontal = 14.dp),
    ) {
        repeat(slots) { index ->
            val building = favorites.getOrNull(index)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(52.dp)
                    .background(Color(0x55302A38), CircleShape)
                    .border(1.dp, Color(0x44FFF6F0), CircleShape)
                    .then(
                        if (building != null) {
                            Modifier.clickable { onFavoriteTap(building) }
                        } else {
                            Modifier
                        },
                    ),
            ) {
                if (building?.icon != null) {
                    val bmp = rememberDrawableBitmap(building)
                    if (bmp != null) {
                        Image(
                            bitmap = bmp,
                            contentDescription = building.label,
                            modifier = Modifier.size(36.dp),
                        )
                    }
                } else {
                    Text(
                        text = "+",
                        color = Color(0x66FFF6F0),
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun rememberDrawableBitmap(building: CityBuilding) =
    androidx.compose.runtime.remember(building.id, building.icon) {
        runCatching { building.icon?.toBitmap(96, 96)?.asImageBitmap() }.getOrNull()
    }
