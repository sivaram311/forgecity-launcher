package buzz.delena.forgecity.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import buzz.delena.forgecity.city.CityBuilding
import buzz.delena.forgecity.city.CityState
import buzz.delena.forgecity.data.CityRepository
import buzz.delena.forgecity.data.ForgeCityDatabase
import buzz.delena.forgecity.launcher.AppCatalog
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

    private val _buildings = MutableStateFlow<List<CityBuilding>>(emptyList())
    val buildings: StateFlow<List<CityBuilding>> = _buildings.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val cityState: StateFlow<CityState> = repository.observeState()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CityState())

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
        refreshApps()
    }

    fun onQueryChange(value: String) {
        _query.value = value
    }

    fun refreshApps() {
        viewModelScope.launch(Dispatchers.Default) {
            _buildings.value = catalog.loadBuildings()
        }
    }

    fun launch(building: CityBuilding) {
        runCatching { catalog.launch(building) }
    }
}
