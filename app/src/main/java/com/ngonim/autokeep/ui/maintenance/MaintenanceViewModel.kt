package com.ngonim.autokeep.ui.maintenance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ngonim.autokeep.data.AutoKeepRepository
import com.ngonim.autokeep.domain.model.AddCustomItem
import com.ngonim.autokeep.domain.model.Dashboard
import com.ngonim.autokeep.domain.model.ServiceCategory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MaintenanceViewModel(
    private val vehicleId: Long,
    private val repository: AutoKeepRepository,
) : ViewModel() {
    val dashboard: StateFlow<Dashboard?> = repository.observeDashboard(vehicleId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun addCustomItem(
        name: String,
        category: ServiceCategory,
        intervalDistance: Int,
        intervalMonths: Int?,
    ) {
        viewModelScope.launch {
            runCatching {
                repository.addCustomItem(
                    AddCustomItem(
                        vehicleId = vehicleId,
                        name = name,
                        category = category,
                        intervalDistance = intervalDistance,
                        intervalMonths = intervalMonths,
                    ),
                )
            }
        }
    }
}
