package com.ngonim.autokeep.ui.service

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ngonim.autokeep.domain.model.ExpenseCategory
import com.ngonim.autokeep.ui.format.epochDayToMillis
import com.ngonim.autokeep.ui.format.formatEpochDay
import com.ngonim.autokeep.ui.format.label
import com.ngonim.autokeep.ui.format.millisToEpochDay
import com.ngonim.autokeep.ui.format.nextServiceLabel
import com.ngonim.autokeep.ui.format.suffix

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordServiceScreen(
    viewModel: RecordServiceViewModel,
    onBack: () -> Unit,
    onFinished: () -> Unit,
) {
    val dashboard by viewModel.dashboard.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDate by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(dashboard?.vehicle?.currentMileage) {
        dashboard?.vehicle?.currentMileage?.let { viewModel.hydrateMileage(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.mode == RecordMode.SERVICE) "Add Service" else "Add Expense") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        val current = dashboard
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = state.mode == RecordMode.SERVICE,
                    onClick = { viewModel.update { it.copy(mode = RecordMode.SERVICE) } },
                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                ) { Text("Service") }
                SegmentedButton(
                    selected = state.mode == RecordMode.EXPENSE,
                    onClick = { viewModel.update { it.copy(mode = RecordMode.EXPENSE) } },
                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                ) { Text("Expense") }
            }

            if (state.mode == RecordMode.SERVICE && current != null) {
                val selectedName = current.items.firstOrNull { it.item.id in state.selectedIds }?.item?.name ?: "Select type"
                ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = it }) {
                    OutlinedTextField(
                        value = selectedName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Service type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    )
                    ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        current.items.forEach { forecast ->
                            DropdownMenuItem(
                                text = { Text(forecast.item.name) },
                                onClick = {
                                    viewModel.selectOnly(forecast.item.id)
                                    typeExpanded = false
                                },
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = state.mileage,
                    onValueChange = { value -> viewModel.update { it.copy(mileage = value) } },
                    label = { Text("Odometer") },
                    suffix = { Text(current.vehicle.mileageUnit.suffix()) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.workshop,
                    onValueChange = { value -> viewModel.update { it.copy(workshop = value) } },
                    label = { Text("Service provider (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                OutlinedTextField(
                    value = state.expenseTitle,
                    onValueChange = { value -> viewModel.update { it.copy(expenseTitle = value) } },
                    label = { Text("Title") },
                    placeholder = { Text("Fuel") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExpenseCategory.entries.forEach { option ->
                        FilterChip(
                            selected = state.expenseCategory == option,
                            onClick = { viewModel.update { it.copy(expenseCategory = option) } },
                            label = { Text(option.label()) },
                        )
                    }
                }
            }

            OutlinedButton(onClick = { showDate = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Date: ${formatEpochDay(state.performedAtEpochDay)}")
            }
            OutlinedTextField(
                value = state.cost,
                onValueChange = { value -> viewModel.update { it.copy(cost = value) } },
                label = { Text(if (state.mode == RecordMode.EXPENSE) "Amount" else "Cost (optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.notes,
                onValueChange = { value -> viewModel.update { it.copy(notes = value) } },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
            )
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(
                onClick = { viewModel.save() },
                enabled = !state.saving,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Text(if (state.saving) "Saving…" else "Save")
            }
        }
    }

    if (showDate) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = epochDayToMillis(state.performedAtEpochDay))
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            viewModel.update { it.copy(performedAtEpochDay = millisToEpochDay(millis)) }
                        }
                        showDate = false
                    },
                ) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDate = false }) { Text("Cancel") } },
        ) { DatePicker(state = pickerState) }
    }

    val saved = state.saved
    if (saved != null && dashboard != null) {
        AlertDialog(
            onDismissRequest = onFinished,
            title = { Text("Service recorded") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    saved.forEach { forecast ->
                        Text("${forecast.item.name}\nNext: ${forecast.nextServiceLabel(dashboard!!.vehicle.mileageUnit)}")
                    }
                }
            },
            confirmButton = { Button(onClick = onFinished) { Text("Done") } },
        )
    }
    if (state.expenseSaved) {
        AlertDialog(
            onDismissRequest = onFinished,
            title = { Text("Expense saved") },
            confirmButton = { Button(onClick = onFinished) { Text("Done") } },
        )
    }
}
