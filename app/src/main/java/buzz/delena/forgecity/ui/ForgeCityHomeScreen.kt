package buzz.delena.forgecity.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import buzz.delena.forgecity.city.CityBuilding
import buzz.delena.forgecity.city.CityState
import androidx.compose.material3.Text

@Composable
fun ForgeCityHomeScreen(
    state: CityState,
    buildings: List<CityBuilding>,
    query: String,
    onQueryChange: (String) -> Unit,
    onBuildingTap: (CityBuilding) -> Unit,
) {
    val filtered = buildings.filter {
        query.isBlank() || it.label.contains(query, ignoreCase = true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1B1624), Color(0xFF0E0C12), Color(0xFF24180F)),
                ),
            ),
    ) {
        CityCanvas(
            buildings = filtered,
            onBuildingTap = onBuildingTap,
            modifier = Modifier.fillMaxSize(),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text(
                text = "ForgeCity",
                color = Color(0xFFFFF6F0),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Chapter ${state.chapterId} · ${state.chapterTitle}",
                color = Color(0xFFE8A15A),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.briefing,
                color = Color(0xD9FFF6F0),
                fontSize = 13.sp,
                lineHeight = 18.sp,
            )
            Spacer(modifier = Modifier.height(12.dp))
            ResourceStrip(state)
            Spacer(modifier = Modifier.height(10.dp))
            SearchBar(query = query, onQueryChange = onQueryChange)
        }

        if (filtered.isEmpty()) {
            Text(
                text = "No buildings match",
                color = Color(0xFFFFF6F0),
                modifier = Modifier.align(Alignment.Center),
            )
        }

        Text(
            text = "Pinch to zoom · drag to pan · tap a building to enter",
            color = Color(0x99FFF6F0),
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 18.dp),
        )
    }
}

@Composable
private fun ResourceStrip(state: CityState) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        ResourceChip("Scrap", state.resources.scrap)
        ResourceChip("Power", state.resources.power)
        ResourceChip("Focus", state.resources.focus)
        ResourceChip("Gold", state.resources.goldDust)
    }
}

@Composable
private fun ResourceChip(label: String, value: Int) {
    Column(
        modifier = Modifier
            .background(Color(0x66302A38), RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Text(text = label, color = Color(0xA6FFF6F0), fontSize = 10.sp)
        Text(
            text = value.toString(),
            color = Color(0xFFFFF6F0),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = TextStyle(color = Color(0xFFFFF6F0), fontSize = 15.sp),
        cursorBrush = SolidColor(Color(0xFFE8A15A)),
        decorationBox = { inner ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(Color(0x99302A38), RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (query.isEmpty()) {
                    Text("Search buildings", color = Color(0x99FFF6F0), fontSize = 15.sp)
                }
                inner()
            }
        },
    )
}
