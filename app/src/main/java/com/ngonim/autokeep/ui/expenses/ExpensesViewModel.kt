package com.ngonim.autokeep.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ngonim.autokeep.data.AutoKeepRepository
import com.ngonim.autokeep.domain.model.ExpenseFilter
import com.ngonim.autokeep.domain.model.ExpenseOverview
import com.ngonim.autokeep.domain.model.Vehicle
import com.ngonim.autokeep.domain.time.Dates
import com.ngonim.autokeep.domain.time.SystemEpochClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class ExpensesViewModel(
    vehicleId: Long,
    repository: AutoKeepRepository,
) : ViewModel() {
    private val year = Dates.yearOf(SystemEpochClock.todayEpochDay())
    private val _filter = MutableStateFlow(ExpenseFilter.ALL)
    val filter: StateFlow<ExpenseFilter> = _filter.asStateFlow()

    val vehicle: StateFlow<Vehicle?> = repository.observeVehicle(vehicleId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val overview: StateFlow<ExpenseOverview> = combine(
        repository.observeExpenseOverview(vehicleId, year),
        _filter,
    ) { data, selected ->
        if (selected == ExpenseFilter.ALL) {
            data
        } else {
            val category = when (selected) {
                ExpenseFilter.FUEL -> com.ngonim.autokeep.domain.model.ExpenseCategory.FUEL
                ExpenseFilter.REPAIRS -> com.ngonim.autokeep.domain.model.ExpenseCategory.REPAIRS
                ExpenseFilter.PARTS -> com.ngonim.autokeep.domain.model.ExpenseCategory.PARTS
                ExpenseFilter.ALL -> com.ngonim.autokeep.domain.model.ExpenseCategory.OTHER
            }
            val filtered = data.entries.filter { it.category == category }
            data.copy(
                totalMinor = filtered.sumOf { it.costMinor },
                entries = filtered,
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ExpenseOverview(year, 0, 0, List(12) { 0L }, emptyList()),
    )

    fun setFilter(value: ExpenseFilter) {
        _filter.value = value
    }
}
