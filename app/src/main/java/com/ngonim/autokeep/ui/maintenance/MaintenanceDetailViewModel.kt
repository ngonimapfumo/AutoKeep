package com.ngonim.autokeep.ui.maintenance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ngonim.autokeep.data.AutoKeepRepository
import com.ngonim.autokeep.domain.model.Dashboard
import com.ngonim.autokeep.domain.model.ServiceForecast
import com.ngonim.autokeep.domain.model.ServiceVisit
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MaintenanceDetailViewModel(
    private val vehicleId: Long,
    private val itemId: Long,
    private val repository: AutoKeepRepository,
) : ViewModel() {
    val dashboard: StateFlow<Dashboard?> = repository.observeDashboard(vehicleId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val forecast: StateFlow<ServiceForecast?> = dashboard
        .map { it?.items?.firstOrNull { row -> row.item.id == itemId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val history: StateFlow<List<ServiceVisit>> = repository.observeHistoryForItem(vehicleId, itemId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun updateInterval(distance: Int, months: Int?) {
        val item = forecast.value?.item ?: return
        viewModelScope.launch {
            runCatching { repository.updateItem(item.copy(intervalDistance = distance, intervalMonths = months)) }
        }
    }

    fun setLastDone(mileage: Int, epochDay: Int) {
        viewModelScope.launch {
            runCatching { repository.setLastDone(itemId, mileage, epochDay) }
        }
    }
}
