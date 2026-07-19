package buzz.delena.forgecity.ui

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import buzz.delena.forgecity.assistant.AssistantUiEvent
import buzz.delena.forgecity.city.CityBuilding
import buzz.delena.forgecity.city.CityState
import buzz.delena.forgecity.city.DayNightCycle
import buzz.delena.forgecity.ui.background.CityBackgroundVideo
import kotlinx.coroutines.delay

@Composable
fun ForgeCityHomeScreen(
    state: CityState,
    buildings: List<CityBuilding>,
    query: String,
    hourOfDay: Int,
    ambientEnabled: Boolean,
    hasUsageAccess: Boolean,
    hasNotificationAccess: Boolean,
    assistantEnabled: Boolean,
    ttsEnabled: Boolean,
    allowCount: Int,
    quietLabel: String,
    backgroundVideoEnabled: Boolean,
    backgroundVideoOpacity: Float,
    showAllowlist: Boolean,
    dockMessage: String?,
    levelUpBuildingId: String?,
    assistantEvent: AssistantUiEvent?,
    onQueryChange: (String) -> Unit,
    onBuildingTap: (CityBuilding) -> Unit,
    onBuildingLongPress: (CityBuilding) -> Unit,
    onFavoriteTap: (CityBuilding) -> Unit,
    onOpenUsageAccess: () -> Unit,
    onOpenNotificationAccess: () -> Unit,
    onToggleAssistant: () -> Unit,
    onToggleTts: () -> Unit,
    onToggleBackgroundVideo: () -> Unit,
    onBackgroundVideoOpacityChange: (Float) -> Unit,
    onQuietStartEarlier: () -> Unit,
    onQuietStartLater: () -> Unit,
    onQuietEndEarlier: () -> Unit,
    onQuietEndLater: () -> Unit,
    onOpenAllowlist: () -> Unit,
    onCloseAllowlist: () -> Unit,
    onToggleAllowedPackage: (String) -> Unit,
    isPackageAllowed: (String) -> Boolean,
    onLevelUpConsumed: () -> Unit,
    onAssistantOpen: (AssistantUiEvent) -> Unit,
    onAssistantDismiss: () -> Unit,
    onClearDockMessage: () -> Unit,
) {
    val filtered = buildings.filter {
        query.isBlank() || it.label.contains(query, ignoreCase = true)
    }
    val favorites = buildings.filter { it.isFavorite }.take(6)
    val (top, mid, bottom) = DayNightCycle.skyColors(hourOfDay)

    LaunchedEffect(dockMessage) {
        if (dockMessage == null) return@LaunchedEffect
        delay(2_200)
        onClearDockMessage()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(top), Color(mid), Color(bottom)),
                ),
            ),
    ) {
        CityBackgroundVideo(
            enabled = ambientEnabled && backgroundVideoEnabled,
            opacity = backgroundVideoOpacity,
            modifier = Modifier.fillMaxSize(),
        )
        if (ambientEnabled && backgroundVideoEnabled) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xA60A0A12),
                                Color(0x660A0A12),
                                Color(0xB30A0A12),
                            ),
                        ),
                    ),
            )
        }
        CityCanvas(
            buildings = filtered,
            hourOfDay = hourOfDay,
            ambientEnabled = ambientEnabled,
            levelUpBuildingId = levelUpBuildingId,
            onBuildingTap = onBuildingTap,
            onBuildingLongPress = onBuildingLongPress,
            onLevelUpConsumed = onLevelUpConsumed,
            modifier = Modifier.fillMaxSize(),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            ChapterCard(state)
            Spacer(modifier = Modifier.height(10.dp))
            ResourceStrip(state)
            if (!hasUsageAccess) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Grant Usage Access to awaken Power / Focus / Gold from real habits →",
                    color = Color(0xFFE8A15A),
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x66302A38), RoundedCornerShape(12.dp))
                        .clickable(onClick = onOpenUsageAccess)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            AssistantSettingsCard(
                hasNotificationAccess = hasNotificationAccess,
                assistantEnabled = assistantEnabled,
                ttsEnabled = ttsEnabled,
                backgroundVideoEnabled = backgroundVideoEnabled,
                backgroundVideoOpacity = backgroundVideoOpacity,
                quietLabel = quietLabel,
                allowCount = allowCount,
                onOpenNotificationAccess = onOpenNotificationAccess,
                onToggleAssistant = onToggleAssistant,
                onToggleTts = onToggleTts,
                onToggleBackgroundVideo = onToggleBackgroundVideo,
                onBackgroundVideoOpacityChange = onBackgroundVideoOpacityChange,
                onQuietStartEarlier = onQuietStartEarlier,
                onQuietStartLater = onQuietStartLater,
                onQuietEndEarlier = onQuietEndEarlier,
                onQuietEndLater = onQuietEndLater,
                onOpenAllowlist = onOpenAllowlist,
            )
            Spacer(modifier = Modifier.height(8.dp))
            SearchBar(query = query, onQueryChange = onQueryChange)
        }

        CityAssistantOverlay(
            event = assistantEvent,
            onOpen = onAssistantOpen,
            onDismiss = onAssistantDismiss,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 100.dp),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            if (dockMessage != null) {
                Text(
                    text = dockMessage,
                    color = Color(0xFFFFF6F0),
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .background(Color(0xAA302A38), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
            FavoritesDock(
                favorites = favorites,
                onFavoriteTap = onFavoriteTap,
            )
            Text(
                text = "Long-press building to pin · pinch / drag city · double-tap recenter",
                color = Color(0x88FFF6F0),
                fontSize = 11.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 6.dp),
            )
        }

        if (showAllowlist) {
            AllowlistSheet(
                buildings = buildings.distinctBy { it.packageName },
                isPackageAllowed = isPackageAllowed,
                onToggle = onToggleAllowedPackage,
                onClose = onCloseAllowlist,
            )
        }
    }
}

@Composable
private fun ChapterCard(state: CityState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    listOf(Color(0xCC2A1838), Color(0xCC301820)),
                ),
                shape = RoundedCornerShape(18.dp),
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(
            text = "ForgeCity",
            color = Color(0xFFFFF6F0),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Chapter ${state.chapterId} · ${state.chapterTitle}",
            color = Color(0xFFE8A15A),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = state.briefing,
            color = Color(0xD9FFF6F0),
            fontSize = 13.sp,
            lineHeight = 18.sp,
            maxLines = 3,
        )
    }
}

@Composable
private fun AllowlistSheet(
    buildings: List<CityBuilding>,
    isPackageAllowed: (String) -> Boolean,
    onToggle: (String) -> Unit,
    onClose: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000))
            .clickable(onClick = onClose),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .heightIn(max = 420.dp)
                .background(Color(0xFF1A1224), RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                .clickable(enabled = false) {}
                .navigationBarsPadding()
                .padding(16.dp),
        ) {
            Text("Apps the assistant may read", color = Color(0xFFFFF6F0), fontWeight = FontWeight.SemiBold)
            Text(
                "Empty by default. Only checked apps can trigger speech/bubbles.",
                color = Color(0x99FFF6F0),
                fontSize = 12.sp,
            )
            Spacer(modifier = Modifier.height(10.dp))
            LazyColumn {
                items(buildings, key = { it.packageName }) { building ->
                    val allowed = isPackageAllowed(building.packageName)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggle(building.packageName) }
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(building.label, color = Color(0xFFFFF6F0), fontSize = 14.sp)
                        Text(if (allowed) "ON" else "OFF", color = Color(0xFFE8A15A), fontSize = 12.sp)
                    }
                }
            }
            Text(
                "Done",
                color = Color(0xFFE8A15A),
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable(onClick = onClose)
                    .padding(8.dp),
            )
        }
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
    val animated by animateIntAsState(
        targetValue = value,
        animationSpec = tween(durationMillis = 700),
        label = "resource-$label",
    )
    Column(
        modifier = Modifier
            .background(Color(0x66302A38), RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Text(text = label, color = Color(0xA6FFF6F0), fontSize = 10.sp)
        Text(
            text = animated.toString(),
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
