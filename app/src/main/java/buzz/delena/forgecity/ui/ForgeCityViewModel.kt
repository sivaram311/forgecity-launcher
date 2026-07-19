package buzz.delena.forgecity.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import buzz.delena.forgecity.assistant.AssistantEventBridge
import buzz.delena.forgecity.assistant.AssistantSettingsStore
import buzz.delena.forgecity.assistant.AssistantUiEvent
import buzz.delena.forgecity.assistant.NotificationAccess
import buzz.delena.forgecity.city.CityBuilding
import buzz.delena.forgecity.city.CityState
import buzz.delena.forgecity.data.CityRepository
import buzz.delena.forgecity.data.ForgeCityDatabase
import buzz.delena.forgecity.launcher.AppCatalog
import buzz.delena.forgecity.power.AnimationBudget
import buzz.delena.forgecity.usage.LaunchTracker
import buzz.delena.forgecity.usage.UsageStatsHarvester
import java.util.Calendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ForgeCityViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = ForgeCityDatabase.get(application).cityDao()
    private val repository = CityRepository(dao)
    private val catalog = AppCatalog(application)
    private val launchTracker = LaunchTracker(dao)
    private val harvester = UsageStatsHarvester(application)
    private val animationBudget = AnimationBudget(application)
    private val assistantSettings = AssistantSettingsStore(application)

    private val _buildings = MutableStateFlow<List<CityBuilding>>(emptyList())
    val buildings: StateFlow<List<CityBuilding>> = _buildings.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _hourOfDay = MutableStateFlow(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
    val hourOfDay: StateFlow<Int> = _hourOfDay.asStateFlow()

    private val _ambientEnabled = MutableStateFlow(animationBudget.allowsAmbient)
    val ambientEnabled: StateFlow<Boolean> = _ambientEnabled.asStateFlow()

    private val _hasUsageAccess = MutableStateFlow(harvester.hasUsageAccess())
    val hasUsageAccess: StateFlow<Boolean> = _hasUsageAccess.asStateFlow()

    private val _hasNotificationAccess = MutableStateFlow(NotificationAccess.hasAccess(application))
    val hasNotificationAccess: StateFlow<Boolean> = _hasNotificationAccess.asStateFlow()

    private val _assistantEnabled = MutableStateFlow(assistantSettings.assistantEnabled)
    val assistantEnabled: StateFlow<Boolean> = _assistantEnabled.asStateFlow()

    private val _ttsEnabled = MutableStateFlow(assistantSettings.ttsEnabled)
    val ttsEnabled: StateFlow<Boolean> = _ttsEnabled.asStateFlow()

    private val _allowCount = MutableStateFlow(assistantSettings.allowedPackages().size)
    val allowCount: StateFlow<Int> = _allowCount.asStateFlow()

    private val _quietLabel = MutableStateFlow(formatQuietLabel())
    val quietLabel: StateFlow<String> = _quietLabel.asStateFlow()

    private val _backgroundVideoEnabled =
        MutableStateFlow(assistantSettings.backgroundVideoEnabled)
    val backgroundVideoEnabled: StateFlow<Boolean> = _backgroundVideoEnabled.asStateFlow()

    private val _backgroundVideoOpacity =
        MutableStateFlow(assistantSettings.backgroundVideoOpacity)
    val backgroundVideoOpacity: StateFlow<Float> = _backgroundVideoOpacity.asStateFlow()

    private val _showAllowlist = MutableStateFlow(false)
    val showAllowlist: StateFlow<Boolean> = _showAllowlist.asStateFlow()

    private val _dockMessage = MutableStateFlow<String?>(null)
    val dockMessage: StateFlow<String?> = _dockMessage.asStateFlow()

    private val _levelUpEvent = MutableStateFlow<String?>(null)
    val levelUpEvent: StateFlow<String?> = _levelUpEvent.asStateFlow()

    private val _assistantEvent = MutableStateFlow<AssistantUiEvent?>(null)
    val assistantEvent: StateFlow<AssistantUiEvent?> = _assistantEvent.asStateFlow()

    val cityState: StateFlow<CityState> = repository.observeState()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CityState())

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
            harvestNow()
        }
        viewModelScope.launch {
            AssistantEventBridge.events.collect { event ->
                _assistantEvent.value = event
            }
        }
        refreshApps()
        refreshEnvironment()
    }

    fun onQueryChange(value: String) {
        _query.value = value
    }

    fun refreshApps() {
        viewModelScope.launch(Dispatchers.Default) {
            val levels = launchTracker.levels()
            val favorites = launchTracker.favorites()
            _buildings.value = catalog.loadBuildings().map { building ->
                building.copy(
                    level = levels[building.id] ?: 1,
                    isFavorite = favorites[building.id] == true,
                )
            }
        }
    }

    fun refreshEnvironment() {
        _hourOfDay.value = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        _ambientEnabled.value = animationBudget.allowsAmbient
        _hasUsageAccess.value = harvester.hasUsageAccess()
        _hasNotificationAccess.value = NotificationAccess.hasAccess(getApplication())
        _assistantEnabled.value = assistantSettings.assistantEnabled
        _ttsEnabled.value = assistantSettings.ttsEnabled
        _allowCount.value = assistantSettings.allowedPackages().size
        _quietLabel.value = formatQuietLabel()
        _backgroundVideoEnabled.value = assistantSettings.backgroundVideoEnabled
        _backgroundVideoOpacity.value = assistantSettings.backgroundVideoOpacity
    }

    fun harvestNow(force: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!harvester.hasUsageAccess()) {
                _hasUsageAccess.value = false
                return@launch
            }
            _hasUsageAccess.value = true
            val now = System.currentTimeMillis()
            if (!force && !repository.shouldHarvest(now, HARVEST_MIN_INTERVAL_MS)) return@launch
            repository.applyUsageGains(harvester.harvestLastHours(24))
            repository.markHarvested(now)
        }
    }

    fun usageAccessIntent() = harvester.usageAccessSettingsIntent()

    fun notificationAccessIntent(): Intent =
        NotificationAccess.settingsIntent(getApplication())

    fun consumeLevelUpEvent() {
        _levelUpEvent.value = null
    }

    fun dismissAssistantEvent() {
        _assistantEvent.value = null
    }

    fun openAssistantEvent(event: AssistantUiEvent) {
        val opened = runCatching {
            event.contentIntent?.send()
            event.contentIntent != null
        }.getOrDefault(false)
        if (!opened) {
            catalog.launchPackage(event.packageName)
        }
        dismissAssistantEvent()
    }

    fun toggleAssistant() {
        assistantSettings.assistantEnabled = !assistantSettings.assistantEnabled
        _assistantEnabled.value = assistantSettings.assistantEnabled
    }

    fun toggleTts() {
        assistantSettings.ttsEnabled = !assistantSettings.ttsEnabled
        _ttsEnabled.value = assistantSettings.ttsEnabled
    }

    fun toggleBackgroundVideo() {
        assistantSettings.backgroundVideoEnabled =
            !assistantSettings.backgroundVideoEnabled
        _backgroundVideoEnabled.value = assistantSettings.backgroundVideoEnabled
    }

    fun setBackgroundVideoOpacity(opacity: Float) {
        assistantSettings.backgroundVideoOpacity = opacity
        _backgroundVideoOpacity.value = assistantSettings.backgroundVideoOpacity
    }

    fun shiftQuietStart(deltaMinutes: Int) {
        assistantSettings.quietStartMinutes =
            wrapDayMinutes(assistantSettings.quietStartMinutes + deltaMinutes)
        refreshEnvironment()
    }

    fun shiftQuietEnd(deltaMinutes: Int) {
        assistantSettings.quietEndMinutes =
            wrapDayMinutes(assistantSettings.quietEndMinutes + deltaMinutes)
        refreshEnvironment()
    }

    fun openAllowlist() {
        _showAllowlist.value = true
    }

    fun closeAllowlist() {
        _showAllowlist.value = false
    }

    fun toggleAllowedPackage(packageName: String) {
        val allowed = assistantSettings.allowedPackages()
        assistantSettings.setPackageAllowed(packageName, packageName !in allowed)
        _allowCount.value = assistantSettings.allowedPackages().size
    }

    fun isPackageAllowed(packageName: String): Boolean =
        packageName in assistantSettings.allowedPackages()

    fun toggleFavorite(building: CityBuilding) {
        viewModelScope.launch {
            val ok = launchTracker.toggleFavorite(building.id)
            _dockMessage.value = when {
                !ok -> "Favorites dock is full (max 6)"
                building.isFavorite -> "Unpinned ${building.label}"
                else -> "Pinned ${building.label}"
            }
            refreshApps()
        }
    }

    fun clearDockMessage() {
        _dockMessage.value = null
    }

    fun launch(building: CityBuilding) {
        runCatching { catalog.launch(building) }
        val previousLevel = building.level
        viewModelScope.launch {
            val newLevel = launchTracker.recordLaunch(building.id)
            if (newLevel > previousLevel) {
                _levelUpEvent.value = building.id
            }
            refreshApps()
        }
    }

    private companion object {
        const val HARVEST_MIN_INTERVAL_MS = 60L * 60L * 1000L

        fun wrapDayMinutes(minutes: Int): Int =
            ((minutes % (24 * 60)) + (24 * 60)) % (24 * 60)
    }

    private fun formatQuietLabel(): String {
        fun fmt(minutes: Int) = "%02d:%02d".format(minutes / 60, minutes % 60)
        return "${fmt(assistantSettings.quietStartMinutes)}–" +
            fmt(assistantSettings.quietEndMinutes)
    }
}
