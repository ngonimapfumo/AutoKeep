package com.ngonim.autokeep.ui.vehicle

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ngonim.autokeep.domain.model.MileageUnit
import com.ngonim.autokeep.domain.model.Powertrain
import com.ngonim.autokeep.domain.model.Transmission
import com.ngonim.autokeep.ui.components.VehicleHero
import com.ngonim.autokeep.ui.components.rememberVehiclePhotoPicker
import com.ngonim.autokeep.ui.format.label
import com.ngonim.autokeep.ui.format.suffix

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVehicleScreen(
    viewModel: AddVehicleViewModel,
    onBack: (() -> Unit)?,
    onSaved: (Long) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pickPhoto = rememberVehiclePhotoPicker { path -> viewModel.setPhoto(path) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add your vehicle") },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            VehicleHero(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clickable(onClick = pickPhoto),
                photoUri = state.photoUri,
                contentDescription = "Vehicle photo",
                showAddHint = true,
            )
            OutlinedTextField(
                value = state.make,
                onValueChange = { value -> viewModel.update { it.copy(make = value) } },
                label = { Text("Make") },
                placeholder = { Text("Honda") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = state.model,
                onValueChange = { value -> viewModel.update { it.copy(model = value) } },
                label = { Text("Model") },
                placeholder = { Text("Grace") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = state.year,
                onValueChange = { value -> viewModel.update { it.copy(year = value) } },
                label = { Text("Year") },
                placeholder = { Text("2015") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            OutlinedTextField(
                value = state.engineSize,
                onValueChange = { value -> viewModel.update { it.copy(engineSize = value) } },
                label = { Text("Engine size (optional)") },
                placeholder = { Text("1.5L") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = state.nickname,
                onValueChange = { value -> viewModel.update { it.copy(nickname = value) } },
                label = { Text("Nickname (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Text("Powertrain", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Powertrain.entries.forEach { option ->
                    FilterChip(
                        selected = state.powertrain == option,
                        onClick = { viewModel.update { it.copy(powertrain = option) } },
                        label = { Text(option.label()) },
                    )
                }
            }
            Text("Transmission", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Transmission.entries.forEach { option ->
                    FilterChip(
                        selected = state.transmission == option,
                        onClick = { viewModel.update { it.copy(transmission = option) } },
                        label = { Text(option.label()) },
                    )
                }
            }
            OutlinedTextField(
                value = state.mileage,
                onValueChange = { value -> viewModel.update { it.copy(mileage = value) } },
                label = { Text("Current mileage") },
                placeholder = { Text("63420") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                suffix = { Text(state.mileageUnit.suffix()) },
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MileageUnit.entries.forEach { option ->
                    FilterChip(
                        selected = state.mileageUnit == option,
                        onClick = { viewModel.update { it.copy(mileageUnit = option) } },
                        label = { Text(option.suffix()) },
                    )
                }
            }
            state.error?.let { error ->
                Text(error, color = MaterialTheme.colorScheme.error)
            }
            Button(
                onClick = { viewModel.save(onSaved) },
                enabled = !state.saving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
            ) {
                Text(if (state.saving) "Setting up…" else "Create checklist")
            }
        }
    }
}
