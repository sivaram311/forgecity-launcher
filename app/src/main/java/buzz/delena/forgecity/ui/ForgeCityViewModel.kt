package buzz.delena.forgecity.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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

    /** Building id that just leveled up; consumed by the canvas to fire a particle burst. */
    private val _levelUpEvent = MutableStateFlow<String?>(null)
    val levelUpEvent: StateFlow<String?> = _levelUpEvent.asStateFlow()

    val cityState: StateFlow<CityState> = repository.observeState()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CityState())

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
            harvestNow()
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
            _buildings.value = catalog.loadBuildings().map { building ->
                building.copy(level = levels[building.id] ?: 1)
            }
        }
    }

    fun refreshEnvironment() {
        _hourOfDay.value = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        _ambientEnabled.value = animationBudget.allowsAmbient
        _hasUsageAccess.value = harvester.hasUsageAccess()
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

    fun consumeLevelUpEvent() {
        _levelUpEvent.value = null
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
    }
}
