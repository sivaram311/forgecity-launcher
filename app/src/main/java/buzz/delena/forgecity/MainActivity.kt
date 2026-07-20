package buzz.delena.forgecity

import android.app.role.RoleManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import buzz.delena.forgecity.ui.ForgeCityHomeScreen
import buzz.delena.forgecity.ui.ForgeCityViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: ForgeCityViewModel by viewModels()
    private var receiverRegistered = false

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            viewModel.refreshApps()
        }
    }

    private val powerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            viewModel.refreshEnvironment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val state by viewModel.cityState.collectAsState()
            val buildings by viewModel.buildings.collectAsState()
            val query by viewModel.query.collectAsState()
            val hour by viewModel.hourOfDay.collectAsState()
            val ambient by viewModel.ambientEnabled.collectAsState()
            val usage by viewModel.hasUsageAccess.collectAsState()
            val notif by viewModel.hasNotificationAccess.collectAsState()
            val assistant by viewModel.assistantEnabled.collectAsState()
            val speechMode by viewModel.speechMode.collectAsState()
            val rewriteEndpoint by viewModel.rewriteEndpoint.collectAsState()
            val apiKeyConfigured by viewModel.apiKeyConfigured.collectAsState()
            val apiKey by viewModel.apiKey.collectAsState()
            val geminiApiKeyConfigured by viewModel.geminiApiKeyConfigured.collectAsState()
            val geminiApiKey by viewModel.geminiApiKey.collectAsState()
            val geminiModel by viewModel.geminiModel.collectAsState()
            val promptTemplate by viewModel.promptTemplate.collectAsState()
            val speechTestStatus by viewModel.speechTestStatus.collectAsState()
            val allowCount by viewModel.allowCount.collectAsState()
            val quietLabel by viewModel.quietLabel.collectAsState()
            val backgroundVideoEnabled by viewModel.backgroundVideoEnabled.collectAsState()
            val backgroundVideoOpacity by viewModel.backgroundVideoOpacity.collectAsState()
            val launcherChromeVisible by viewModel.launcherChromeVisible.collectAsState()
            val assistantToolsVisible by viewModel.assistantToolsVisible.collectAsState()
            val showAllowlist by viewModel.showAllowlist.collectAsState()
            val dockMessage by viewModel.dockMessage.collectAsState()
            val levelUp by viewModel.levelUpEvent.collectAsState()
            val assistantEvent by viewModel.assistantEvent.collectAsState()
            ForgeCityHomeScreen(
                state = state,
                buildings = buildings,
                query = query,
                hourOfDay = hour,
                ambientEnabled = ambient,
                hasUsageAccess = usage,
                hasNotificationAccess = notif,
                assistantEnabled = assistant,
                speechMode = speechMode,
                rewriteEndpoint = rewriteEndpoint,
                apiKeyConfigured = apiKeyConfigured,
                apiKey = apiKey,
                geminiApiKeyConfigured = geminiApiKeyConfigured,
                geminiApiKey = geminiApiKey,
                geminiModel = geminiModel,
                promptTemplate = promptTemplate,
                speechTestStatus = speechTestStatus,
                allowCount = allowCount,
                quietLabel = quietLabel,
                backgroundVideoEnabled = backgroundVideoEnabled,
                backgroundVideoOpacity = backgroundVideoOpacity,
                launcherChromeVisible = launcherChromeVisible,
                assistantToolsVisible = assistantToolsVisible,
                showAllowlist = showAllowlist,
                dockMessage = dockMessage,
                levelUpBuildingId = levelUp,
                assistantEvent = assistantEvent,
                onQueryChange = viewModel::onQueryChange,
                onBuildingTap = viewModel::launch,
                onBuildingLongPress = viewModel::toggleFavorite,
                onFavoriteTap = viewModel::launch,
                onOpenUsageAccess = { startActivity(viewModel.usageAccessIntent()) },
                onOpenNotificationAccess = { startActivity(viewModel.notificationAccessIntent()) },
                onToggleAssistant = viewModel::toggleAssistant,
                onCycleSpeechMode = viewModel::cycleSpeechMode,
                onRewriteEndpointChange = viewModel::setRewriteEndpoint,
                onSaveApiKey = viewModel::saveApiKey,
                onGeminiModelChange = viewModel::setGeminiModel,
                onPromptTemplateChange = viewModel::setPromptTemplate,
                onSaveGeminiApiKey = viewModel::saveGeminiApiKey,
                onTestSpeechMode = viewModel::testSpeechMode,
                onClearSpeechTestStatus = viewModel::clearSpeechTestStatus,
                onToggleBackgroundVideo = viewModel::toggleBackgroundVideo,
                onBackgroundVideoOpacityChange = viewModel::setBackgroundVideoOpacity,
                onToggleLauncherChrome = viewModel::toggleLauncherChrome,
                onToggleAssistantTools = viewModel::toggleAssistantTools,
                onQuietStartEarlier = { viewModel.shiftQuietStart(-30) },
                onQuietStartLater = { viewModel.shiftQuietStart(30) },
                onQuietEndEarlier = { viewModel.shiftQuietEnd(-30) },
                onQuietEndLater = { viewModel.shiftQuietEnd(30) },
                onOpenAllowlist = viewModel::openAllowlist,
                onCloseAllowlist = viewModel::closeAllowlist,
                onToggleAllowedPackage = viewModel::toggleAllowedPackage,
                isPackageAllowed = viewModel::isPackageAllowed,
                onLevelUpConsumed = viewModel::consumeLevelUpEvent,
                onAssistantOpen = viewModel::openAssistantEvent,
                onAssistantDismiss = viewModel::dismissAssistantEvent,
                onClearDockMessage = viewModel::clearDockMessage,
            )
        }
        maybeRequestHomeRole()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshEnvironment()
        viewModel.harvestNow()
    }

    override fun onStart() {
        super.onStart()
        if (!receiverRegistered) {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_CHANGED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
                addDataScheme("package")
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(packageReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                @Suppress("DEPRECATION")
                registerReceiver(packageReceiver, filter)
            }
            val powerFilter = IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(powerReceiver, powerFilter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                @Suppress("DEPRECATION")
                registerReceiver(powerReceiver, powerFilter)
            }
            receiverRegistered = true
        }
    }

    override fun onStop() {
        if (receiverRegistered) {
            unregisterReceiver(packageReceiver)
            unregisterReceiver(powerReceiver)
            receiverRegistered = false
        }
        super.onStop()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != HOME_ROLE_REQUEST) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java) ?: return
            if (!roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {
                Toast.makeText(this, R.string.set_as_home, Toast.LENGTH_LONG).show()
                startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
            }
        }
    }

    private fun maybeRequestHomeRole() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        val roleManager = getSystemService(RoleManager::class.java) ?: return
        if (roleManager.isRoleAvailable(RoleManager.ROLE_HOME) &&
            !roleManager.isRoleHeld(RoleManager.ROLE_HOME)
        ) {
            @Suppress("DEPRECATION")
            startActivityForResult(
                roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME),
                HOME_ROLE_REQUEST,
            )
        }
    }

    companion object {
        private const val HOME_ROLE_REQUEST = 210
    }
}
