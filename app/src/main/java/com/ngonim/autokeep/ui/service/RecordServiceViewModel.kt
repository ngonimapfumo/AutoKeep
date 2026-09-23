package com.ngonim.autokeep.ui.service

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ngonim.autokeep.data.AutoKeepRepository
import com.ngonim.autokeep.domain.model.AddExpense
import com.ngonim.autokeep.domain.model.Dashboard
import com.ngonim.autokeep.domain.model.ExpenseCategory
import com.ngonim.autokeep.domain.model.RecordService
import com.ngonim.autokeep.domain.model.ServiceForecast
import com.ngonim.autokeep.domain.time.Dates
import com.ngonim.autokeep.ui.format.parseMoneyToMinor
import com.ngonim.autokeep.ui.navigation.Routes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class RecordMode { SERVICE, EXPENSE }

data class RecordServiceUiState(
    val mode: RecordMode = RecordMode.SERVICE,
    val selectedIds: Set<Long> = emptySet(),
    val mileage: String = "",
    val cost: String = "",
    val workshop: String = "",
    val notes: String = "",
    val expenseTitle: String = "",
    val expenseCategory: ExpenseCategory = ExpenseCategory.FUEL,
    val receiptUri: String? = null,
    val performedAtEpochDay: Int = Dates.todayEpochDay(),
    val saving: Boolean = false,
    val error: String? = null,
    val saved: List<ServiceForecast>? = null,
    val expenseSaved: Boolean = false,
)

class RecordServiceViewModel(
    private val vehicleId: Long,
    preselectedItemId: Long,
    initialMode: RecordMode,
    private val repository: AutoKeepRepository,
) : ViewModel() {
    val dashboard: StateFlow<Dashboard?> = repository.observeDashboard(vehicleId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _state = MutableStateFlow(
        RecordServiceUiState(
            mode = initialMode,
            selectedIds = if (preselectedItemId > 0) setOf(preselectedItemId) else emptySet(),
        ),
    )
    val state: StateFlow<RecordServiceUiState> = _state.asStateFlow()

    fun hydrateMileage(currentMileage: Int) {
        _state.update { current ->
            if (current.mileage.isBlank()) current.copy(mileage = currentMileage.toString()) else current
        }
    }

    fun selectOnly(itemId: Long) {
        _state.update { it.copy(selectedIds = setOf(itemId), error = null) }
    }

    fun update(transform: (RecordServiceUiState) -> RecordServiceUiState) {
        _state.update { transform(it).copy(error = null) }
    }

    fun save() {
        if (_state.value.mode == RecordMode.EXPENSE) {
            saveExpense()
        } else {
            saveService()
        }
    }

    private fun saveService() {
        val current = _state.value
        val mileage = current.mileage.replace(",", "").toIntOrNull()
        if (current.selectedIds.isEmpty()) {
            _state.update { it.copy(error = "Choose a service type") }
            return
        }
        if (mileage == null) {
            _state.update { it.copy(error = "Enter the odometer reading") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(saving = true) }
            runCatching {
                repository.recordService(
                    RecordService(
                        vehicleId = vehicleId,
                        serviceItemIds = current.selectedIds,
                        mileage = mileage,
                        performedAtEpochDay = current.performedAtEpochDay,
                        costMinor = parseMoneyToMinor(current.cost),
                        workshop = current.workshop,
                        notes = current.notes,
                        receiptUri = current.receiptUri,
                    ),
                )
            }.onSuccess { forecasts ->
                _state.update { it.copy(saving = false, saved = forecasts) }
            }.onFailure { error ->
                _state.update { it.copy(saving = false, error = error.message ?: "Could not save") }
            }
        }
    }

    private fun saveExpense() {
        val current = _state.value
        val amount = parseMoneyToMinor(current.cost)
        val title = current.expenseTitle.ifBlank { current.expenseCategory.name.lowercase().replaceFirstChar { it.titlecase() } }
        if (amount == null || amount <= 0) {
            _state.update { it.copy(error = "Enter the amount") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(saving = true) }
            runCatching {
                repository.addExpense(
                    AddExpense(
                        vehicleId = vehicleId,
                        title = title,
                        category = current.expenseCategory,
                        costMinor = amount,
                        performedAtEpochDay = current.performedAtEpochDay,
                        notes = current.notes,
                    ),
                )
            }.onSuccess {
                _state.update { it.copy(saving = false, expenseSaved = true) }
            }.onFailure { error ->
                _state.update { it.copy(saving = false, error = error.message ?: "Could not save") }
            }
        }
    }

    companion object {
        fun modeFrom(route: String?): RecordMode =
            if (route == Routes.MODE_EXPENSE) RecordMode.EXPENSE else RecordMode.SERVICE
    }
}
