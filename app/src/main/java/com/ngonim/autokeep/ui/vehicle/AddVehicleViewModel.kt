package com.ngonim.autokeep.ui.vehicle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ngonim.autokeep.data.AutoKeepRepository
import com.ngonim.autokeep.data.VehiclePhotoStore
import com.ngonim.autokeep.domain.model.AddVehicle
import com.ngonim.autokeep.domain.model.MileageUnit
import com.ngonim.autokeep.domain.model.Powertrain
import com.ngonim.autokeep.domain.model.Transmission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddVehicleUiState(
    val make: String = "",
    val model: String = "",
    val year: String = "",
    val engineSize: String = "",
    val nickname: String = "",
    val mileage: String = "",
    val powertrain: Powertrain = Powertrain.GASOLINE,
    val transmission: Transmission = Transmission.AUTOMATIC,
    val mileageUnit: MileageUnit = MileageUnit.KILOMETERS,
    val photoUri: String? = null,
    val saving: Boolean = false,
    val error: String? = null,
)

class AddVehicleViewModel(
    private val repository: AutoKeepRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(AddVehicleUiState())
    val state: StateFlow<AddVehicleUiState> = _state.asStateFlow()

    fun update(transform: (AddVehicleUiState) -> AddVehicleUiState) {
        _state.update { transform(it).copy(error = null) }
    }

    fun setPhoto(path: String) {
        _state.update { current ->
            if (current.photoUri != path) VehiclePhotoStore.delete(current.photoUri)
            current.copy(photoUri = path, error = null)
        }
    }

    fun save(onSaved: (Long) -> Unit) {
        val current = _state.value
        val mileage = current.mileage.replace(",", "").toIntOrNull()
        val year = current.year.trim().takeIf { it.isNotEmpty() }?.toIntOrNull()
        if (current.make.isBlank() || current.model.isBlank()) {
            _state.update { it.copy(error = "Add the make and model") }
            return
        }
        if (mileage == null || mileage < 0) {
            _state.update { it.copy(error = "Enter the current mileage") }
            return
        }
        if (current.year.isNotBlank() && year == null) {
            _state.update { it.copy(error = "Year looks off") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(saving = true, error = null) }
            runCatching {
                repository.addVehicle(
                    AddVehicle(
                        make = current.make,
                        model = current.model,
                        year = year,
                        engineSize = current.engineSize,
                        powertrain = current.powertrain,
                        transmission = current.transmission,
                        currentMileage = mileage,
                        mileageUnit = current.mileageUnit,
                        nickname = current.nickname,
                        photoUri = current.photoUri,
                    ),
                )
            }.onSuccess { id ->
                _state.update { it.copy(saving = false) }
                onSaved(id)
            }.onFailure { error ->
                _state.update { it.copy(saving = false, error = error.message ?: "Could not save") }
            }
        }
    }
}
