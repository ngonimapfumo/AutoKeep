package com.ngonim.autokeep.ui.service

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ngonim.autokeep.data.AutoKeepRepository
import com.ngonim.autokeep.domain.model.ServiceVisit
import com.ngonim.autokeep.domain.model.Vehicle
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ServiceHistoryViewModel(
    vehicleId: Long,
    repository: AutoKeepRepository,
) : ViewModel() {
    val vehicle: StateFlow<Vehicle?> = repository.observeVehicle(vehicleId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val visits: StateFlow<List<ServiceVisit>> = repository.observeHistory(vehicleId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
