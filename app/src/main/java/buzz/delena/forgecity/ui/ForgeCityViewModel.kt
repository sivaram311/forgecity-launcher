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
    private val repository = CityRepository(ForgeCityDatabase.get(application).cityDao())
    private val catalog = AppCatalog(application)
    private val launchTracker = LaunchTracker(application)
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
            _buildings.value = catalog.loadBuildings().map { building ->
                building.copy(level = launchTracker.levelFor(building.id))
            }
        }
    }

    fun refreshEnvironment() {
        _hourOfDay.value = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        _ambientEnabled.value = animationBudget.allowsAmbient
        _hasUsageAccess.value = harvester.hasUsageAccess()
    }

    fun harvestNow() {
        viewModelScope.launch(Dispatchers.IO) {
            if (!harvester.hasUsageAccess()) return@launch
            repository.applyUsageGains(harvester.harvestLastHours(24))
            _hasUsageAccess.value = true
        }
    }

    fun usageAccessIntent() = harvester.usageAccessSettingsIntent()

    fun launch(building: CityBuilding) {
        launchTracker.recordLaunch(building.id)
        runCatching { catalog.launch(building) }
        refreshApps()
    }
}
