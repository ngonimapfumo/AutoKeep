package com.ngonim.autokeep.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ngonim.autokeep.data.AutoKeepRepository
import com.ngonim.autokeep.domain.model.Dashboard
import com.ngonim.autokeep.domain.model.YearlyCosts
import com.ngonim.autokeep.domain.time.Dates
import com.ngonim.autokeep.domain.time.SystemEpochClock
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val vehicleId: Long,
    private val repository: AutoKeepRepository,
) : ViewModel() {
    val dashboard: StateFlow<Dashboard?> = repository.observeDashboard(vehicleId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val yearlyCosts: StateFlow<YearlyCosts> = repository
        .observeYearlyCosts(vehicleId, Dates.yearOf(SystemEpochClock.todayEpochDay()))
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            YearlyCosts(Dates.yearOf(SystemEpochClock.todayEpochDay()), 0, emptyMap()),
        )

    fun updateMileage(mileage: Int) {
        viewModelScope.launch {
            runCatching { repository.updateMileage(vehicleId, mileage) }
        }
    }

    fun updatePhoto(photoUri: String?) {
        viewModelScope.launch {
            runCatching { repository.updatePhoto(vehicleId, photoUri) }
        }
    }

    fun deleteVehicle(onDeleted: () -> Unit) {
        viewModelScope.launch {
            runCatching { repository.deleteVehicle(vehicleId) }
                .onSuccess { onDeleted() }
        }
    }
}
