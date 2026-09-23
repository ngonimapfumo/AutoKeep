package com.ngonim.autokeep.ui.maintenance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ngonim.autokeep.domain.model.ServiceCategory
import com.ngonim.autokeep.ui.components.ServiceRow
import com.ngonim.autokeep.ui.format.label
import com.ngonim.autokeep.ui.format.suffix

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceScreen(
    viewModel: MaintenanceViewModel,
    onBack: () -> Unit,
    onItem: (Long) -> Unit,
    onRecordService: () -> Unit,
) {
    val dashboard by viewModel.dashboard.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Maintenance") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = onRecordService) { Text("Record") }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add custom item")
            }
        },
    ) { innerPadding ->
        val current = dashboard
        if (current == null) {
            Text("Loading…", modifier = Modifier.padding(innerPadding).padding(24.dp))
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        ) {
            item {
                Text(
                    text = "Intervals are a starting point. Tap an item to adjust them.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            items(current.items, key = { it.item.id }) { forecast ->
                ServiceRow(
                    forecast = forecast,
                    unit = current.vehicle.mileageUnit,
                    onClick = { onItem(forecast.item.id) },
                )
            }
        }
    }

    if (showAdd) {
        AddCustomItemDialog(
            unitSuffix = dashboard?.vehicle?.mileageUnit?.suffix().orEmpty(),
            onDismiss = { showAdd = false },
            onConfirm = { name, category, distance, months ->
                viewModel.addCustomItem(name, category, distance, months)
                showAdd = false
            },
        )
    }
}

@Composable
private fun AddCustomItemDialog(
    unitSuffix: String,
    onDismiss: () -> Unit,
    onConfirm: (String, ServiceCategory, Int, Int?) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }
    var months by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ServiceCategory.OTHER) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = distance,
                    onValueChange = { distance = it },
                    label = { Text("Interval") },
                    suffix = { Text(unitSuffix) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = months,
                    onValueChange = { months = it },
                    label = { Text("Months (optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ServiceCategory.entries.forEach { option ->
                        FilterChip(
                            selected = category == option,
                            onClick = { category = option },
                            label = { Text(option.label()) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val interval = distance.toIntOrNull()
                    if (name.isNotBlank() && interval != null && interval > 0) {
                        onConfirm(name, category, interval, months.toIntOrNull())
                    }
                },
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
