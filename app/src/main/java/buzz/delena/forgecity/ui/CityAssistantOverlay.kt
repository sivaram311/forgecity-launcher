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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import buzz.delena.forgecity.assistant.AssistantSpeechMode
import buzz.delena.forgecity.assistant.AssistantUiEvent
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

@Composable
fun AssistantSettingsCard(
    hasNotificationAccess: Boolean,
    assistantEnabled: Boolean,
    speechMode: AssistantSpeechMode,
    rewriteEndpoint: String,
    apiKeyConfigured: Boolean,
    apiKey: String,
    speechTestStatus: String?,
    backgroundVideoEnabled: Boolean,
    backgroundVideoOpacity: Float,
    quietLabel: String,
    allowCount: Int,
    onOpenNotificationAccess: () -> Unit,
    onToggleAssistant: () -> Unit,
    onCycleSpeechMode: () -> Unit,
    onRewriteEndpointChange: (String) -> Unit,
    onSaveApiKey: (String) -> Unit,
    onTestSpeechMode: () -> Unit,
    onClearSpeechTestStatus: () -> Unit,
    onToggleBackgroundVideo: () -> Unit,
    onBackgroundVideoOpacityChange: (Float) -> Unit,
    onQuietStartEarlier: () -> Unit,
    onQuietStartLater: () -> Unit,
    onQuietEndEarlier: () -> Unit,
    onQuietEndLater: () -> Unit,
    onOpenAllowlist: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var apiKeyDraft by remember(apiKey) { mutableStateOf(apiKey) }
    LaunchedEffect(speechTestStatus) {
        if (speechTestStatus == null) return@LaunchedEffect
        delay(8_000)
        onClearSpeechTestStatus()
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
                },
                color = Color(0xFFE8A15A),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Text(
            text = "Tap to cycle: off → device-locale direct TTS → Agent Portal Tamil. " +
                "Portal mode uses store=false and fails closed silently.",
            color = Color(0x88FFF6F0),
            fontSize = 10.sp,
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
                "Save key",
                color = Color(0xFFE8A15A),
                fontSize = 11.sp,
                modifier = Modifier
                    .clickable {
                        onSaveApiKey(apiKeyDraft)
                    }
                    .padding(8.dp),
            )
        }
        Text(
            "Saved key is visible here for setup and encrypted at rest by Android Keystore. " +
                "Do not share screenshots. Saving an empty field clears it.",
            color = Color(0x77FFF6F0),
            fontSize = 9.sp,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Test current speech mode",
                color = Color(0xFFFFF6F0),
                fontSize = 11.sp,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "TEST TTS",
                color = Color(0xFF4FD1C5),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable(onClick = onTestSpeechMode)
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
        Text(
            "Diagnosis: adb logcat -s ForgeCityTTS",
            color = Color(0x77FFF6F0),
            fontSize = 9.sp,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Atmosphere",
            color = Color(0xFFE8A15A),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
        )
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
private fun RemoteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
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
