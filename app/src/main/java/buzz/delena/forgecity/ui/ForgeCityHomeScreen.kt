package buzz.delena.forgecity.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import buzz.delena.forgecity.assistant.AssistantSpeechMode
import buzz.delena.forgecity.assistant.AssistantUiEvent
import buzz.delena.forgecity.city.CityBuilding
import buzz.delena.forgecity.city.CityState
import buzz.delena.forgecity.city.DayNightCycle
import buzz.delena.forgecity.house.AppPlacementEngine
import buzz.delena.forgecity.house.HouseFeatureFlags
import buzz.delena.forgecity.house.HouseRoom as DomainHouseRoom
import buzz.delena.forgecity.house.PlaceableApp
import buzz.delena.forgecity.ui.background.CityBackgroundVideo
import buzz.delena.forgecity.ui.house.HouseHomeSurface
import buzz.delena.forgecity.ui.house.HouseLabelMarker
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
    speechMode: AssistantSpeechMode,
    rewriteEndpoint: String,
    apiKeyConfigured: Boolean,
    apiKey: String,
    geminiApiKeyConfigured: Boolean,
    geminiApiKey: String,
    geminiModel: String,
    geminiVoice: String,
    geminiLanguageCode: String,
    promptTemplate: String,
    speechTestStatus: String?,
    diagnosticsLog: String,
    allowCount: Int,
    quietLabel: String,
    backgroundVideoEnabled: Boolean,
    backgroundVideoOpacity: Float,
    launcherChromeVisible: Boolean,
    assistantPanelVisible: Boolean,
    searchBarVisible: Boolean,
    dockPanelVisible: Boolean,
    speechTestText: String,
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
    onCycleSpeechMode: () -> Unit,
    onRewriteEndpointChange: (String) -> Unit,
    onSaveApiKey: (String) -> Unit,
    onGeminiModelChange: (String) -> Unit,
    onGeminiVoiceChange: (String) -> Unit,
    onGeminiLanguageCodeChange: (String) -> Unit,
    onPromptTemplateChange: (String) -> Unit,
    onSaveGeminiApiKey: (String) -> Unit,
    onTestSpeechMode: () -> Unit,
    onClearSpeechTestStatus: () -> Unit,
    onClearDiagnosticsLog: () -> Unit,
    onToggleBackgroundVideo: () -> Unit,
    onBackgroundVideoOpacityChange: (Float) -> Unit,
    onToggleLauncherChrome: () -> Unit,
    onToggleAssistantPanel: () -> Unit,
    onToggleSearchBar: () -> Unit,
    onToggleDockPanel: () -> Unit,
    onSpeechTestTextChange: (String) -> Unit,
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
    val houseMode = HouseFeatureFlags.use3dHouse
    val (top, mid, bottom) = DayNightCycle.skyColors(hourOfDay)
    val houseBackdrop = listOf(Color(0xFF1A1410), Color(0xFF2C2118), Color(0xFF1E1612))

    // Slice D2: when the query resolves to a single building, fly the camera to it.
    val focusBuildingId = if (query.isNotBlank() && filtered.size == 1) filtered.first().id else null
    val houseMarkers = remember(filtered) { demoHouseMarkers(filtered) }

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
                    if (houseMode) houseBackdrop else listOf(Color(top), Color(mid), Color(bottom)),
                ),
            ),
    ) {
        // House mode must not force city video; compile flag gates the Media3 path.
        val videoOn = !houseMode &&
            ambientEnabled &&
            backgroundVideoEnabled &&
            HouseFeatureFlags.useCityVideo
        CityBackgroundVideo(
            enabled = videoOn,
            opacity = backgroundVideoOpacity,
            modifier = Modifier.fillMaxSize(),
        )

        // Slice C1: instead of a full-screen mud scrim, fade the video into the
        // city mid so the iso city owns the lower half while the skyline stays visible.
        if (videoOn) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Transparent,
                                0.46f to Color.Transparent,
                                0.72f to Color(0x66060510),
                                1.0f to Color(0xCC050409),
                            ),
                        ),
                    ),
            )
        }

        if (houseMode) {
            HouseHomeSurface(
                markers = houseMarkers,
                ambientEnabled = ambientEnabled,
                night = DayNightCycle.isNight(hourOfDay),
                onMarkerTap = { marker ->
                    filtered.firstOrNull { it.id == marker.id }?.let(onBuildingTap)
                },
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            CityCanvas(
                buildings = filtered,
                hourOfDay = hourOfDay,
                ambientEnabled = ambientEnabled,
                levelUpBuildingId = levelUpBuildingId,
                focusBuildingId = focusBuildingId,
                onBuildingTap = onBuildingTap,
                onBuildingLongPress = onBuildingLongPress,
                onLevelUpConsumed = onLevelUpConsumed,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Slice C4: subtle vignette so the poster/gradient state reads with depth.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color(0x3305040A)),
                        radius = 1600f,
                    ),
                ),
        )

        // Slice A1/C: local top scrim strip only behind the top chrome.
        AnimatedVisibility(
            visible = launcherChromeVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xB3070510), Color(0x00070510)),
                        ),
                    ),
            )
        }

        // Slice A1/C: local bottom scrim strip only behind the favorites dock.
        AnimatedVisibility(
            visible = launcherChromeVisible && dockPanelVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0x00060510), Color(0xC0060510)),
                        ),
                    ),
            )
        }

        AnimatedVisibility(
            visible = launcherChromeVisible,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                ChapterPill(state)
                Spacer(modifier = Modifier.height(8.dp))
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
                if (searchBarVisible) {
                    Spacer(modifier = Modifier.height(8.dp))
                    SearchBar(query = query, onQueryChange = onQueryChange)
                }
            }
        }

        AnimatedVisibility(
            visible = launcherChromeVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 100.dp),
        ) {
            CityAssistantOverlay(
                event = assistantEvent,
                onOpen = onAssistantOpen,
                onDismiss = onAssistantDismiss,
            )
        }

        AnimatedVisibility(
            visible = launcherChromeVisible && dockPanelVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Column {
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
            }
        }

        // Slice A2: assistant settings live in a modal sheet, not the home scroll.
        if (launcherChromeVisible && assistantPanelVisible) {
            AssistantSettingsSheet(
                onClose = onToggleAssistantPanel,
                hasNotificationAccess = hasNotificationAccess,
                assistantEnabled = assistantEnabled,
                speechMode = speechMode,
                rewriteEndpoint = rewriteEndpoint,
                apiKeyConfigured = apiKeyConfigured,
                apiKey = apiKey,
                geminiApiKeyConfigured = geminiApiKeyConfigured,
                geminiApiKey = geminiApiKey,
                geminiModel = geminiModel,
                geminiVoice = geminiVoice,
                geminiLanguageCode = geminiLanguageCode,
                promptTemplate = promptTemplate,
                speechTestText = speechTestText,
                speechTestStatus = speechTestStatus,
                diagnosticsLog = diagnosticsLog,
                backgroundVideoEnabled = backgroundVideoEnabled,
                backgroundVideoOpacity = backgroundVideoOpacity,
                quietLabel = quietLabel,
                allowCount = allowCount,
                onOpenNotificationAccess = onOpenNotificationAccess,
                onToggleAssistant = onToggleAssistant,
                onCycleSpeechMode = onCycleSpeechMode,
                onRewriteEndpointChange = onRewriteEndpointChange,
                onSaveApiKey = onSaveApiKey,
                onGeminiModelChange = onGeminiModelChange,
                onGeminiVoiceChange = onGeminiVoiceChange,
                onGeminiLanguageCodeChange = onGeminiLanguageCodeChange,
                onPromptTemplateChange = onPromptTemplateChange,
                onSaveGeminiApiKey = onSaveGeminiApiKey,
                onSpeechTestTextChange = onSpeechTestTextChange,
                onTestSpeechMode = onTestSpeechMode,
                onClearSpeechTestStatus = onClearSpeechTestStatus,
                onClearDiagnosticsLog = onClearDiagnosticsLog,
                onToggleBackgroundVideo = onToggleBackgroundVideo,
                onBackgroundVideoOpacityChange = onBackgroundVideoOpacityChange,
                onQuietStartEarlier = onQuietStartEarlier,
                onQuietStartLater = onQuietStartLater,
                onQuietEndEarlier = onQuietEndEarlier,
                onQuietEndLater = onQuietEndLater,
                onOpenAllowlist = onOpenAllowlist,
            )
        }

        if (launcherChromeVisible && assistantPanelVisible && showAllowlist) {
            AllowlistSheet(
                buildings = buildings.distinctBy { it.packageName },
                isPackageAllowed = isPackageAllowed,
                onToggle = onToggleAllowedPackage,
                onClose = onCloseAllowlist,
            )
        }

        ChromeMenu(
            launcherChromeVisible = launcherChromeVisible,
            assistantPanelVisible = assistantPanelVisible,
            searchBarVisible = searchBarVisible,
            dockPanelVisible = dockPanelVisible,
            onToggleLauncherChrome = onToggleLauncherChrome,
            onToggleAssistantPanel = onToggleAssistantPanel,
            onToggleSearchBar = onToggleSearchBar,
            onToggleDockPanel = onToggleDockPanel,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 6.dp, end = 6.dp),
        )
    }
}

/**
 * Slice A4: a single accessible overflow menu replacing the four text chips.
 * Keeps the ability to hide chrome, open assistant settings, and toggle search / dock.
 */
@Composable
private fun ChromeMenu(
    launcherChromeVisible: Boolean,
    assistantPanelVisible: Boolean,
    searchBarVisible: Boolean,
    dockPanelVisible: Boolean,
    onToggleLauncherChrome: () -> Unit,
    onToggleAssistantPanel: () -> Unit,
    onToggleSearchBar: () -> Unit,
    onToggleDockPanel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xCC201828), RoundedCornerShape(16.dp))
                .semantics {
                    contentDescription = "Launcher options"
                    stateDescription = if (expanded) "Menu open" else "Menu closed"
                }
                .clickable(role = Role.Button) { expanded = true },
        ) {
            Canvas(modifier = Modifier.size(20.dp)) {
                val barColor = Color(0xFFFFF6F0)
                val w = size.width
                repeat(3) { i ->
                    val y = size.height * (0.24f + i * 0.26f)
                    drawLine(
                        color = barColor,
                        start = Offset(w * 0.12f, y),
                        end = Offset(w * 0.88f, y),
                        strokeWidth = 2.2f * density,
                        cap = StrokeCap.Round,
                    )
                }
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            ChromeMenuItem(
                label = "Launcher UI",
                enabled = launcherChromeVisible,
            ) {
                expanded = false
                onToggleLauncherChrome()
            }
            ChromeMenuItem(
                label = "Assistant settings",
                enabled = assistantPanelVisible,
            ) {
                expanded = false
                onToggleAssistantPanel()
            }
            ChromeMenuItem(
                label = "Search",
                enabled = searchBarVisible,
            ) {
                expanded = false
                onToggleSearchBar()
            }
            ChromeMenuItem(
                label = "Favorites dock",
                enabled = dockPanelVisible,
            ) {
                expanded = false
                onToggleDockPanel()
            }
        }
    }
}

@Composable
private fun ChromeMenuItem(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(label, fontSize = 13.sp) },
        trailingIcon = {
            Text(
                text = if (enabled) "ON" else "OFF",
                color = if (enabled) Color(0xFF4FD1C5) else Color(0x99FFF6F0),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
            )
        },
        modifier = Modifier.semantics {
            stateDescription = if (enabled) "$label shown" else "$label hidden"
        },
        onClick = onClick,
    )
}

/**
 * Slice A3: compact chapter pill by default; tapping expands the briefing.
 * Expand state is intentionally in-memory only.
 */
@Composable
private fun ChapterPill(state: CityState) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    listOf(Color(0xCC2A1838), Color(0xCC301820)),
                ),
                shape = RoundedCornerShape(16.dp),
            )
            .clickable { expanded = !expanded }
            .animateContentSize()
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "ForgeCity",
                color = Color(0xFFFFF6F0),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Ch ${state.chapterId} · ${state.chapterTitle}",
                color = Color(0xFFE8A15A),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
            )
            Text(
                text = if (expanded) "▾" else "▸",
                color = Color(0x99FFF6F0),
                fontSize = 13.sp,
            )
        }
        if (expanded) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = state.briefing,
                color = Color(0xD9FFF6F0),
                fontSize = 13.sp,
                lineHeight = 18.sp,
            )
        }
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
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        ResourceChip("Scrap", state.resources.scrap, Modifier.weight(1f))
        ResourceChip("Power", state.resources.power, Modifier.weight(1f))
        ResourceChip("Focus", state.resources.focus, Modifier.weight(1f))
        ResourceChip("Gold", state.resources.goldDust, Modifier.weight(1f))
    }
}

@Composable
private fun ResourceChip(label: String, value: Int, modifier: Modifier = Modifier) {
    val animated by animateIntAsState(
        targetValue = value,
        animationSpec = tween(durationMillis = 700),
        label = "resource-$label",
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = modifier
            .background(Color(0x66302A38), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp),
    ) {
        Text(text = label, color = Color(0xA6FFF6F0), fontSize = 10.sp)
        Text(
            text = animated.toString(),
            color = Color(0xFFFFF6F0),
            fontSize = 13.sp,
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
                    .height(44.dp)
                    .background(Color(0x99302A38), RoundedCornerShape(22.dp))
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

/** Place apps via AppPlacementEngine; map domain rooms onto Wave-1 floor-plan ids. */
private fun demoHouseMarkers(buildings: List<CityBuilding>): List<HouseLabelMarker> {
    val byId = buildings.associateBy { it.id }
    val apps = buildings.map { b ->
        PlaceableApp(
            id = b.id,
            district = b.district,
            isFavorite = b.isFavorite,
            launchCount = b.level.coerceAtLeast(0),
        )
    }
    return AppPlacementEngine.place(apps).mapNotNull { placement ->
        val building = byId[placement.appId] ?: return@mapNotNull null
        val bounds = placement.hotspot.room.bounds
        val nx = ((placement.hotspot.localOffset.x / bounds.width).coerceIn(0.15f, 0.85f))
        val ny = ((placement.hotspot.localOffset.z / bounds.depth).coerceIn(0.2f, 0.8f))
        HouseLabelMarker(
            id = building.id,
            label = building.label,
            roomId = uiRoomId(placement.hotspot.room),
            nx = nx,
            ny = ny,
        )
    }
}

/** Wave-1 floor plan has 6 cells; Vault domain room shares Office visually for now. */
private fun uiRoomId(room: DomainHouseRoom): String = when (room) {
    DomainHouseRoom.KITCHEN -> "kitchen"
    DomainHouseRoom.LIVING -> "living"
    DomainHouseRoom.HALLWAY -> "hallway"
    DomainHouseRoom.OFFICE, DomainHouseRoom.VAULT -> "office"
    DomainHouseRoom.BEDROOM -> "bedroom"
    DomainHouseRoom.WORKSHOP -> "workshop"
}
