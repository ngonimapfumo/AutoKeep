package com.ngonim.autokeep.ui.garage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ngonim.autokeep.data.AutoKeepRepository
import com.ngonim.autokeep.domain.health.HealthCalculator
import com.ngonim.autokeep.domain.model.Vehicle
import com.ngonim.autokeep.domain.model.VehicleHealthBadge
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GarageVehicle(
    val vehicle: Vehicle,
    val badge: VehicleHealthBadge,
)

@OptIn(ExperimentalCoroutinesApi::class)
class GarageViewModel(
    private val repository: AutoKeepRepository,
) : ViewModel() {
    val vehicles: StateFlow<List<GarageVehicle>> = repository.observeVehicles()
        .flatMapLatest { vehicles ->
            if (vehicles.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(vehicles.map { vehicle ->
                    repository.observeDashboard(vehicle.id)
                }) { dashboards ->
                    vehicles.mapIndexed { index, vehicle ->
                        GarageVehicle(
                            vehicle = vehicle,
                            badge = dashboards[index]?.let { HealthCalculator.badge(it) }
                                ?: VehicleHealthBadge.ALL_GOOD,
                        )
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun deleteVehicle(vehicleId: Long) {
        viewModelScope.launch {
            runCatching { repository.deleteVehicle(vehicleId) }
        }
    }
}
