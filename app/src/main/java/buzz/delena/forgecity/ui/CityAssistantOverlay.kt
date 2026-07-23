package buzz.delena.forgecity.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Slider
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import buzz.delena.forgecity.assistant.AssistantSpeechMode
import buzz.delena.forgecity.assistant.AssistantUiEvent
import buzz.delena.forgecity.assistant.gemini.AudioPromptPresets
import buzz.delena.forgecity.assistant.gemini.GeminiTtsCatalog
import buzz.delena.forgecity.assistant.gemini.GeminiVoiceSelection
import buzz.delena.forgecity.assistant.gemini.PromptModeValidator
import buzz.delena.forgecity.assistant.gemini.PromptTemplateEntry
import kotlinx.coroutines.delay

@Composable
fun CityAssistantOverlay(
    event: AssistantUiEvent?,
    onOpen: (AssistantUiEvent) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(event?.notificationKey) {
        if (event == null) return@LaunchedEffect
        delay(8_000)
        onDismiss()
    }

    AnimatedVisibility(
        visible = event != null,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 },
        modifier = modifier,
    ) {
        val current = event ?: return@AnimatedVisibility
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .pointerInput(current.notificationKey) {
                    detectHorizontalDragGestures { _, dragAmount ->
                        if (kotlin.math.abs(dragAmount) > 40f) onDismiss()
                    }
                },
        ) {
            Canvas(modifier = Modifier.size(56.dp)) {
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0xFF4FD1FF), Color(0xFFFF9A4A), Color(0x00000000)),
                    ),
                    radius = size.minDimension * 0.55f,
                    center = Offset(size.width * 0.5f, size.height * 0.55f),
                )
                drawCircle(Color(0xFF1A2038), radius = size.minDimension * 0.28f)
                drawCircle(Color(0xFF4FD1FF), radius = 4f, center = Offset(size.width * 0.38f, size.height * 0.45f))
                drawCircle(Color(0xFFFF9A4A), radius = 4f, center = Offset(size.width * 0.62f, size.height * 0.45f))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xEE241830), RoundedCornerShape(18.dp))
                    .clickable { onOpen(current) }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Text(
                    text = current.appLabel,
                    color = Color(0xFFE8A15A),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = current.title.ifBlank { "Notification" },
                    color = Color(0xFFFFF6F0),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
                if (current.shortText.isNotBlank()) {
                    Text(
                        text = current.shortText,
                        color = Color(0xCCFFF6F0),
                        fontSize = 12.sp,
                        maxLines = 2,
                    )
                }
                Text(
                    text = "Tap to open · swipe to dismiss",
                    color = Color(0x88FFF6F0),
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

/**
 * Slice A2: modal sheet host for the assistant settings so they no longer live
 * in the permanent home scroll. All existing fields/callbacks are preserved.
 */
@Composable
fun AssistantSettingsSheet(
    onClose: () -> Unit,
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
    promptTemplates: List<PromptTemplateEntry> = emptyList(),
    activePromptTemplateId: String = "",
    speechTestText: String,
    speechTestStatus: String?,
    diagnosticsLog: String,
    backgroundVideoEnabled: Boolean,
    backgroundVideoOpacity: Float,
    houseHomeToggleVisible: Boolean = false,
    houseHomeEnabled: Boolean = true,
    quietLabel: String,
    allowCount: Int,
    onOpenNotificationAccess: () -> Unit,
    onToggleAssistant: () -> Unit,
    onCycleSpeechMode: () -> Unit,
    onRewriteEndpointChange: (String) -> Unit,
    onSaveApiKey: (String) -> Unit,
    onGeminiModelChange: (String) -> Unit,
    onGeminiVoiceChange: (String) -> Unit,
    onGeminiLanguageCodeChange: (String) -> Unit,
    onPromptTemplateChange: (String) -> Unit,
    onSelectPromptTemplate: (String) -> Unit = {},
    onSavePromptTemplateAs: (String) -> Unit = {},
    onDeletePromptTemplate: (String) -> Unit = {},
    onSaveGeminiApiKey: (String) -> Unit,
    onSpeechTestTextChange: (String) -> Unit,
    onTestSpeechMode: () -> Unit,
    onClearSpeechTestStatus: () -> Unit,
    onClearDiagnosticsLog: () -> Unit,
    onToggleBackgroundVideo: () -> Unit,
    onToggleHouseHome: () -> Unit = {},
    onBackgroundVideoOpacityChange: (Float) -> Unit,
    onQuietStartEarlier: () -> Unit,
    onQuietStartLater: () -> Unit,
    onQuietEndEarlier: () -> Unit,
    onQuietEndLater: () -> Unit,
    onOpenAllowlist: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xAA000000))
            .clickable(onClick = onClose),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .background(
                    Color(0xFF14101E),
                    RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
                )
                .clickable(enabled = false) {}
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    "City Assistant settings",
                    color = Color(0xFFFFF6F0),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "Close",
                    color = Color(0xFFE8A15A),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable(onClick = onClose)
                        .padding(8.dp),
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                AssistantSettingsCard(
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
                    promptTemplates = promptTemplates,
                    activePromptTemplateId = activePromptTemplateId,
                    speechTestText = speechTestText,
                    speechTestStatus = speechTestStatus,
                    diagnosticsLog = diagnosticsLog,
                    backgroundVideoEnabled = backgroundVideoEnabled,
                    backgroundVideoOpacity = backgroundVideoOpacity,
                    houseHomeToggleVisible = houseHomeToggleVisible,
                    houseHomeEnabled = houseHomeEnabled,
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
                    onSelectPromptTemplate = onSelectPromptTemplate,
                    onSavePromptTemplateAs = onSavePromptTemplateAs,
                    onDeletePromptTemplate = onDeletePromptTemplate,
                    onSaveGeminiApiKey = onSaveGeminiApiKey,
                    onSpeechTestTextChange = onSpeechTestTextChange,
                    onTestSpeechMode = onTestSpeechMode,
                    onClearSpeechTestStatus = onClearSpeechTestStatus,
                    onClearDiagnosticsLog = onClearDiagnosticsLog,
                    onToggleBackgroundVideo = onToggleBackgroundVideo,
                    onToggleHouseHome = onToggleHouseHome,
                    onBackgroundVideoOpacityChange = onBackgroundVideoOpacityChange,
                    onQuietStartEarlier = onQuietStartEarlier,
                    onQuietStartLater = onQuietStartLater,
                    onQuietEndEarlier = onQuietEndEarlier,
                    onQuietEndLater = onQuietEndLater,
                    onOpenAllowlist = onOpenAllowlist,
                )
            }
        }
    }
}

@Composable
fun AssistantSettingsCard(
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
    promptTemplates: List<PromptTemplateEntry> = emptyList(),
    activePromptTemplateId: String = "",
    speechTestText: String,
    speechTestStatus: String?,
    diagnosticsLog: String,
    backgroundVideoEnabled: Boolean,
    backgroundVideoOpacity: Float,
    houseHomeToggleVisible: Boolean = false,
    houseHomeEnabled: Boolean = true,
    quietLabel: String,
    allowCount: Int,
    onOpenNotificationAccess: () -> Unit,
    onToggleAssistant: () -> Unit,
    onCycleSpeechMode: () -> Unit,
    onRewriteEndpointChange: (String) -> Unit,
    onSaveApiKey: (String) -> Unit,
    onGeminiModelChange: (String) -> Unit,
    onGeminiVoiceChange: (String) -> Unit,
    onGeminiLanguageCodeChange: (String) -> Unit,
    onPromptTemplateChange: (String) -> Unit,
    onSelectPromptTemplate: (String) -> Unit = {},
    onSavePromptTemplateAs: (String) -> Unit = {},
    onDeletePromptTemplate: (String) -> Unit = {},
    onSaveGeminiApiKey: (String) -> Unit,
    onSpeechTestTextChange: (String) -> Unit,
    onTestSpeechMode: () -> Unit,
    onClearSpeechTestStatus: () -> Unit,
    onClearDiagnosticsLog: () -> Unit,
    onToggleBackgroundVideo: () -> Unit,
    onToggleHouseHome: () -> Unit = {},
    onBackgroundVideoOpacityChange: (Float) -> Unit,
    onQuietStartEarlier: () -> Unit,
    onQuietStartLater: () -> Unit,
    onQuietEndEarlier: () -> Unit,
    onQuietEndLater: () -> Unit,
    onOpenAllowlist: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var apiKeyDraft by remember(apiKey) { mutableStateOf(apiKey) }
    var geminiKeyDraft by remember(geminiApiKey) { mutableStateOf(geminiApiKey) }
    var templateDraft by remember(promptTemplate) { mutableStateOf(promptTemplate) }
    var saveAsName by remember { mutableStateOf("") }
    var showSaveAs by remember { mutableStateOf(false) }
    var copyHint by remember { mutableStateOf<String?>(null) }
    var revealPortalKey by remember { mutableStateOf(false) }
    var revealGeminiKey by remember { mutableStateOf(false) }
    var diagnosticsExpanded by remember { mutableStateOf(true) }
    val clipboard = LocalClipboardManager.current
    LaunchedEffect(promptTemplate) { templateDraft = promptTemplate }
    val promptValidation = remember(speechMode, templateDraft) {
        PromptModeValidator.validate(speechMode, templateDraft)
    }
    val testEnabled = remember(speechMode, templateDraft) {
        PromptModeValidator.canRunTest(speechMode, templateDraft)
    }
    val showGeminiCreds = speechMode == AssistantSpeechMode.GEMINI_TAMIL ||
        speechMode == AssistantSpeechMode.SMART_CASCADE
    val showPortalCreds = speechMode == AssistantSpeechMode.AGENT_PORTAL_TAMIL ||
        speechMode == AssistantSpeechMode.SMART_CASCADE
    val showAudioPrompt = showGeminiCreds
    LaunchedEffect(speechTestStatus) {
        if (speechTestStatus == null) return@LaunchedEffect
        delay(8_000)
        onClearSpeechTestStatus()
    }
    LaunchedEffect(copyHint) {
        if (copyHint == null) return@LaunchedEffect
        delay(2_000)
        copyHint = null
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0x99201828), RoundedCornerShape(16.dp))
            .padding(12.dp),
    ) {
        Text("City Assistant", color = Color(0xFFFFF6F0), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(6.dp))
        if (!hasNotificationAccess) {
            Text(
                text = "Grant Notification Access so the robot can announce alerts →",
                color = Color(0xFFE8A15A),
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenNotificationAccess)
                    .padding(vertical = 6.dp),
            )
        }
        SettingRow("Assistant visible", assistantEnabled, onToggleAssistant)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onCycleSpeechMode)
                .padding(vertical = 6.dp),
        ) {
            Text("Speech mode", color = Color(0xFFFFF6F0), fontSize = 12.sp)
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = when (speechMode) {
                    AssistantSpeechMode.OFF -> "OFF"
                    AssistantSpeechMode.DIRECT_TTS -> "DIRECT"
                    AssistantSpeechMode.AGENT_PORTAL_TAMIL -> "PORTAL தமிழ்"
                    AssistantSpeechMode.GEMINI_TAMIL -> "GEMINI AUDIO"
                    AssistantSpeechMode.SMART_CASCADE -> "CASCADE Audio→Portal→TTS"
                },
                color = Color(0xFFE8A15A),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Text(
            text = "Tap to cycle: OFF → DIRECT → PORTAL → GEMINI AUDIO → CASCADE.",
            color = Color(0x88FFF6F0),
            fontSize = 10.sp,
        )

        if (showAudioPrompt) {
            Text(
                text = "Speech engine — audio speak prompt",
                color = Color(0xFFE8A15A),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = "Native audio needs speak-aloud text (not a rewrite script).",
                color = Color(0x88FFF6F0),
                fontSize = 9.sp,
            )
            CatalogDropdown(
                label = promptTemplates.firstOrNull { it.id == activePromptTemplateId }?.name
                    ?: "Template",
                options = promptTemplates.map { it.id to it.name },
                onSelect = { id ->
                    onSelectPromptTemplate(id)
                    promptTemplates.firstOrNull { it.id == id }?.let {
                        templateDraft = it.body
                    }
                },
            )
            Row(
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
            ) {
                AudioPromptPresets.ALL.forEach { preset ->
                    Text(
                        text = preset.label,
                        color = Color(0xFF4FD1C5),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .background(Color(0x55302A38), RoundedCornerShape(8.dp))
                            .clickable {
                                val match = promptTemplates.firstOrNull { it.id == preset.id }
                                if (match != null) {
                                    onSelectPromptTemplate(match.id)
                                    templateDraft = match.body
                                } else {
                                    templateDraft = preset.template
                                    onPromptTemplateChange(preset.template)
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                    )
                }
            }
            RemoteMultilineField(
                value = templateDraft,
                onValueChange = {
                    templateDraft = it
                    onPromptTemplateChange(it)
                },
                placeholder = "Synthesize speech… #### TRANSCRIPT … {appLabel} {title} {text}",
                minLines = 4,
            )
            Row(
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp),
            ) {
                Text(
                    text = if (showSaveAs) "Cancel" else "Save as…",
                    color = Color(0xFF4FD1C5),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable { showSaveAs = !showSaveAs }
                        .padding(4.dp),
                )
                if (promptTemplates.size > 1) {
                    Text(
                        text = "Delete",
                        color = Color(0xFFFF8A80),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable { onDeletePromptTemplate(activePromptTemplateId) }
                            .padding(4.dp),
                    )
                }
            }
            if (showSaveAs) {
                RemoteTextField(
                    value = saveAsName,
                    onValueChange = { saveAsName = it },
                    placeholder = "Template name",
                )
                Text(
                    text = "Save",
                    color = Color(0xFFE8A15A),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable {
                            if (saveAsName.isNotBlank()) {
                                onSavePromptTemplateAs(saveAsName.trim())
                                saveAsName = ""
                                showSaveAs = false
                            }
                        }
                        .padding(4.dp),
                )
            }
            if (!promptValidation.ok && promptValidation.message != null) {
                Text(
                    text = promptValidation.message!!,
                    color = Color(0xFFFF8A80),
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            } else {
                Text(
                    text = "Placeholders OK: {appLabel} {title} {text} {maxChars}",
                    color = Color(0x77FFF6F0),
                    fontSize = 9.sp,
                )
            }
        }

        if (showGeminiCreds) {
            Text(
                text = "Gemini credentials",
                color = Color(0xFFE8A15A),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp),
            )
            CatalogDropdown(
                label = GeminiTtsCatalog.models.firstOrNull { it.id == geminiModel }?.label
                    ?: geminiModel,
                options = GeminiTtsCatalog.models.map { it.id to it.label },
                onSelect = onGeminiModelChange,
            )
            val voiceOptions = buildList {
                add(GeminiVoiceSelection.SENTINEL_RANDOM to "Random")
                add(GeminiVoiceSelection.SENTINEL_RANDOM_FEMALE to "Random female")
                add(GeminiVoiceSelection.SENTINEL_RANDOM_MALE to "Random male")
                GeminiTtsCatalog.voices.forEach { add(it.name to it.label) }
            }
            CatalogDropdown(
                label = GeminiVoiceSelection.displayLabel(geminiVoice),
                options = voiceOptions,
                onSelect = onGeminiVoiceChange,
            )
            RemoteTextField(
                value = geminiLanguageCode,
                onValueChange = onGeminiLanguageCodeChange,
                placeholder = "ta-IN (prompt hint only)",
            )
            RemoteTextField(
                value = geminiKeyDraft,
                onValueChange = { geminiKeyDraft = it },
                placeholder = "Gemini API key",
                masked = !revealGeminiKey,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    if (geminiApiKeyConfigured) "Gemini key configured" else "No Gemini key saved",
                    color = if (geminiApiKeyConfigured) Color(0xFF4FD1C5) else Color(0x99FFF6F0),
                    fontSize = 10.sp,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    if (revealGeminiKey) "Hide" else "Reveal",
                    color = Color(0xFF4FD1C5),
                    fontSize = 11.sp,
                    modifier = Modifier
                        .clickable { revealGeminiKey = !revealGeminiKey }
                        .padding(8.dp),
                )
                Text(
                    "Save Gemini key",
                    color = Color(0xFFE8A15A),
                    fontSize = 11.sp,
                    modifier = Modifier
                        .clickable { onSaveGeminiApiKey(geminiKeyDraft) }
                        .padding(8.dp),
                )
            }
        }

        if (showPortalCreds) {
            Text(
                text = "Agent Portal credentials",
                color = Color(0xFFE8A15A),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp),
            )
            RemoteTextField(
                value = rewriteEndpoint,
                onValueChange = onRewriteEndpointChange,
                placeholder = "https://portal.example/api/forgecity/rewrite",
            )
            Spacer(modifier = Modifier.height(4.dp))
            RemoteTextField(
                value = apiKeyDraft,
                onValueChange = { apiKeyDraft = it },
                placeholder = "X-ForgeCity-Key",
                masked = !revealPortalKey,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    if (apiKeyConfigured) "API key configured" else "No API key saved",
                    color = if (apiKeyConfigured) Color(0xFF4FD1C5) else Color(0x99FFF6F0),
                    fontSize = 10.sp,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    if (revealPortalKey) "Hide" else "Reveal",
                    color = Color(0xFF4FD1C5),
                    fontSize = 11.sp,
                    modifier = Modifier
                        .clickable { revealPortalKey = !revealPortalKey }
                        .padding(8.dp),
                )
                Text(
                    "Save Portal key",
                    color = Color(0xFFE8A15A),
                    fontSize = 11.sp,
                    modifier = Modifier
                        .clickable { onSaveApiKey(apiKeyDraft) }
                        .padding(8.dp),
                )
            }
            Text(
                "Keys stay Keystore-encrypted at rest. Empty save clears. Avoid screenshots.",
                color = Color(0x77FFF6F0),
                fontSize = 9.sp,
            )
        }

        Text(
            text = "TEST TTS",
            color = Color(0xFFE8A15A),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 8.dp),
        )
        RemoteMultilineField(
            value = speechTestText,
            onValueChange = onSpeechTestTextChange,
            placeholder = "Custom notification text for TEST TTS",
            minLines = 2,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = if (testEnabled) "Test current speech mode" else "Fix prompt / pick a mode",
                color = Color(0xFFFFF6F0),
                fontSize = 11.sp,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "TEST TTS",
                color = if (testEnabled) Color(0xFF4FD1C5) else Color(0x554FD1C5),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable(enabled = testEnabled, onClick = onTestSpeechMode)
                    .padding(8.dp),
            )
        }
        if (speechTestStatus != null) {
            Text(
                text = speechTestStatus,
                color = Color(0xFFE8A15A),
                fontSize = 10.sp,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { diagnosticsExpanded = !diagnosticsExpanded }
                .padding(top = 8.dp),
        ) {
            Text(
                text = "Speech diagnostics log",
                color = Color(0xFFE8A15A),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = if (diagnosticsExpanded) "Hide" else "Show",
                color = Color(0xFF4FD1C5),
                fontSize = 10.sp,
            )
        }
        if (diagnosticsExpanded) {
            Text(
                text = "Safe events only (no keys / notification text). COPY → paste to agent.",
                color = Color(0x77FFF6F0),
                fontSize = 9.sp,
            )
            BasicTextField(
                value = diagnosticsLog.ifBlank { "(empty — run TEST TTS or wait for a spoken notification)" },
                onValueChange = {},
                readOnly = true,
                textStyle = TextStyle(
                    color = Color(0xFFB8F0E8),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .heightIn(min = 72.dp, max = 160.dp)
                    .background(Color(0xFF0C0A12), RoundedCornerShape(8.dp))
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState()),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "COPY LOG",
                    color = Color(0xFF4FD1C5),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable {
                            val text = diagnosticsLog.trim()
                            if (text.isEmpty()) {
                                copyHint = "Log empty"
                            } else {
                                clipboard.setText(AnnotatedString(text))
                                copyHint = "Copied — paste to agent"
                            }
                        }
                        .padding(8.dp),
                )
                Text(
                    text = "CLEAR",
                    color = Color(0xFFE8A15A),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable(onClick = onClearDiagnosticsLog)
                        .padding(8.dp),
                )
                Spacer(modifier = Modifier.weight(1f))
                if (copyHint != null) {
                    Text(
                        text = copyHint!!,
                        color = Color(0xFF4FD1C5),
                        fontSize = 9.sp,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Atmosphere",
            color = Color(0xFFE8A15A),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
        )
        if (houseHomeToggleVisible) {
            SettingRow("3D House home", houseHomeEnabled, onToggleHouseHome)
        }
        SettingRow("Background Video", backgroundVideoEnabled, onToggleBackgroundVideo)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Opacity", color = Color(0xAAFFF6F0), fontSize = 10.sp)
            Slider(
                value = backgroundVideoOpacity.coerceIn(0.4f, 1f),
                onValueChange = onBackgroundVideoOpacityChange,
                valueRange = 0.4f..1f,
                enabled = backgroundVideoEnabled,
                modifier = Modifier
                    .weight(1f)
                    .height(28.dp)
                    .padding(horizontal = 8.dp),
            )
            Text(
                text = "${(backgroundVideoOpacity * 100).toInt()}%",
                color = Color(0xFFE8A15A),
                fontSize = 10.sp,
            )
        }
        Text(
            text = "Quiet hours $quietLabel",
            color = Color(0xAAFFF6F0),
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 4.dp),
        )
        Row(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            QuietButton("Start −", onQuietStartEarlier)
            QuietButton("Start +", onQuietStartLater)
            QuietButton("End −", onQuietEndEarlier)
            QuietButton("End +", onQuietEndLater)
        }
        Text(
            text = "Allowlist $allowCount apps →",
            color = Color(0xAAFFF6F0),
            fontSize = 11.sp,
            modifier = Modifier
                .clickable(onClick = onOpenAllowlist)
                .padding(top = 4.dp, bottom = 2.dp),
        )
        Text(
            text = "Privacy defaults: speech off, allowlist empty. " +
                "Notification text and Tamil replies are never persisted or logged.",
            color = Color(0x77FFF6F0),
            fontSize = 10.sp,
        )
    }
}

@Composable
private fun CatalogDropdown(
    label: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
    ) {
        Text(
            text = label,
            color = Color(0xFFFFF6F0),
            fontSize = 11.sp,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0x66302A38), RoundedCornerShape(8.dp))
                .clickable { expanded = true }
                .padding(horizontal = 10.dp, vertical = 10.dp),
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .background(Color(0xFF241830)),
        ) {
            options.forEach { (id, optionLabel) ->
                DropdownMenuItem(
                    text = {
                        Text(optionLabel, color = Color(0xFFFFF6F0), fontSize = 11.sp)
                    },
                    onClick = {
                        onSelect(id)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun RemoteMultilineField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    minLines: Int = 3,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = TextStyle(color = Color(0xFFFFF6F0), fontSize = 10.sp),
        cursorBrush = SolidColor(Color(0xFFE8A15A)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
            .heightIn(min = (minLines * 16).dp)
            .background(Color(0x66302A38), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 7.dp),
        decorationBox = { inner ->
            Box(contentAlignment = Alignment.TopStart) {
                if (value.isEmpty()) {
                    Text(placeholder, color = Color(0x66FFF6F0), fontSize = 10.sp)
                }
                inner()
            }
        },
    )
}

@Composable
private fun RemoteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    masked: Boolean = false,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        visualTransformation = if (masked) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        textStyle = TextStyle(color = Color(0xFFFFF6F0), fontSize = 10.sp),
        cursorBrush = SolidColor(Color(0xFFE8A15A)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
            .background(Color(0x66302A38), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 7.dp),
        decorationBox = { inner ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text(placeholder, color = Color(0x66FFF6F0), fontSize = 10.sp)
                }
                inner()
            }
        },
    )
}

@Composable
private fun QuietButton(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        color = Color(0xFFE8A15A),
        fontSize = 10.sp,
        modifier = Modifier
            .background(Color(0x44302A38), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 7.dp, vertical = 6.dp),
    )
}

@Composable
private fun SettingRow(label: String, enabled: Boolean, onToggle: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(if (enabled) Color(0xFF4FD1C5) else Color(0x55FFF6F0), CircleShape),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, color = Color(0xFFFFF6F0), fontSize = 12.sp)
        Spacer(modifier = Modifier.weight(1f))
        Text(if (enabled) "ON" else "OFF", color = Color(0xFFE8A15A), fontSize = 11.sp)
    }
}
