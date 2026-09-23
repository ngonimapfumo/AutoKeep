package com.ngonim.autokeep.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ngonim.autokeep.domain.model.HealthStatus
import com.ngonim.autokeep.ui.components.HealthCountChip
import com.ngonim.autokeep.ui.components.ServiceRow
import com.ngonim.autokeep.ui.format.formatMoney
import com.ngonim.autokeep.ui.format.formatMileage
import com.ngonim.autokeep.ui.format.suffix
import com.ngonim.autokeep.ui.format.vehicleMeta

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onBack: () -> Unit,
    onMaintenance: () -> Unit,
    onHistory: () -> Unit,
    onRecordService: () -> Unit,
    onItem: (Long) -> Unit,
) {
    val dashboard by viewModel.dashboard.collectAsStateWithLifecycle()
    val costs by viewModel.yearlyCosts.collectAsStateWithLifecycle()
    var showMileage by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(dashboard?.vehicle?.displayName ?: "Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Garage")
                    }
                },
                actions = {
                    TextButton(onClick = onHistory) { Text("History") }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onRecordService) {
                Icon(Icons.Filled.Add, contentDescription = "Record service")
            }
        },
    ) { innerPadding ->
        val current = dashboard
        if (current == null) {
            Text("Loading…", modifier = Modifier.padding(innerPadding).padding(24.dp))
            return@Scaffold
        }
        val unit = current.vehicle.mileageUnit
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 88.dp),
        ) {
            item {
                Text(
                    text = vehicleMeta(
                        current.vehicle.year,
                        current.vehicle.powertrain,
                        current.vehicle.transmission,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = formatMileage(current.vehicle.currentMileage, unit),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    TextButton(onClick = { showMileage = true }) { Text("Update") }
                }
                Spacer(Modifier.height(12.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HealthCountChip(HealthStatus.GOOD, current.goodCount)
                    HealthCountChip(HealthStatus.DUE_SOON, current.dueSoonCount)
                    HealthCountChip(HealthStatus.OVERDUE, current.overdueCount)
                    if (current.unknownCount > 0) {
                        HealthCountChip(HealthStatus.UNKNOWN, current.unknownCount)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Card(onClick = onHistory, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Maintenance this year", style = MaterialTheme.typography.labelLarge)
                        Text(
                            text = formatMoney(costs.totalMinor, current.vehicle.currencyCode),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onMaintenance) { Text("See full checklist") }
            }
            if (current.needsAttention.isNotEmpty()) {
                item {
                    Text(
                        text = "Needs attention",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                items(current.needsAttention, key = { it.item.id }) { forecast ->
                    ServiceRow(
                        forecast = forecast,
                        unit = unit,
                        onClick = { onItem(forecast.item.id) },
                    )
                }
            }
        }
    }

    if (showMileage) {
        val currentMileage = dashboard?.vehicle?.currentMileage?.toString().orEmpty()
        MileageDialog(
            initial = currentMileage,
            unitSuffix = dashboard?.vehicle?.mileageUnit?.suffix().orEmpty(),
            onDismiss = { showMileage = false },
            onConfirm = { value ->
                value.toIntOrNull()?.let { viewModel.updateMileage(it) }
                showMileage = false
            },
        )
    }
}

@Composable
private fun MileageDialog(
    initial: String,
    unitSuffix: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update mileage") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                suffix = { Text(unitSuffix) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
            )
        },
        confirmButton = { Button(onClick = { onConfirm(text) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
