package com.ngonim.autokeep.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ngonim.autokeep.data.AutoKeepRepository
import com.ngonim.autokeep.data.UserPreferences
import com.ngonim.autokeep.domain.health.HealthCalculator
import com.ngonim.autokeep.domain.model.Dashboard
import com.ngonim.autokeep.domain.model.ServiceForecast
import com.ngonim.autokeep.domain.model.ServiceVisit
import com.ngonim.autokeep.domain.model.Vehicle
import com.ngonim.autokeep.domain.model.VehicleHealthBadge
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

data class HomeUiState(
    val greetingName: String,
    val greeting: String,
    val vehicle: Vehicle? = null,
    val badge: VehicleHealthBadge = VehicleHealthBadge.ALL_GOOD,
    val nextService: ServiceForecast? = null,
    val lastService: ServiceVisit? = null,
    val totalSpendMinor: Long = 0,
    val reminders: List<ServiceForecast> = emptyList(),
    val dashboard: Dashboard? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    repository: AutoKeepRepository,
    userPreferences: UserPreferences,
) : ViewModel() {
    val state: StateFlow<HomeUiState> = repository.observeVehicles()
        .flatMapLatest { vehicles ->
            val primary = vehicles.firstOrNull()
            val greetingName = userPreferences.displayName.substringBefore(' ').ifBlank { "" }
            if (primary == null) {
                flowOf(HomeUiState(greetingName = greetingName, greeting = greeting()))
            } else {
                combine(
                    repository.observeDashboard(primary.id),
                    repository.observeHistory(primary.id),
                    repository.observeTotalSpend(primary.id),
                ) { dashboard, history, spend ->
                    HomeUiState(
                        greetingName = greetingName,
                        greeting = greeting(),
                        vehicle = primary,
                        badge = dashboard?.let { HealthCalculator.badge(it) } ?: VehicleHealthBadge.ALL_GOOD,
                        nextService = dashboard?.let { HealthCalculator.nextDue(it.items) },
                        lastService = history.firstOrNull(),
                        totalSpendMinor = spend,
                        reminders = dashboard?.needsAttention.orEmpty().take(3),
                        dashboard = dashboard,
                    )
                }
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            HomeUiState(greetingName = userPreferences.displayName.substringBefore(' '), greeting = greeting()),
        )

    private fun greeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }
    }
}
