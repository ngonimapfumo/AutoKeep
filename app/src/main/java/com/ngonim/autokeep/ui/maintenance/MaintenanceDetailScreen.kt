package com.ngonim.autokeep.ui.maintenance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.ngonim.autokeep.domain.model.MileageUnit
import com.ngonim.autokeep.domain.model.ServiceVisit
import com.ngonim.autokeep.domain.time.Dates
import com.ngonim.autokeep.ui.components.HealthDot
import com.ngonim.autokeep.ui.components.color
import com.ngonim.autokeep.ui.format.formatEpochDay
import com.ngonim.autokeep.ui.format.formatMileage
import com.ngonim.autokeep.ui.format.formatMoney
import com.ngonim.autokeep.ui.format.label
import com.ngonim.autokeep.ui.format.nextServiceLabel
import com.ngonim.autokeep.ui.format.remainingLabel
import com.ngonim.autokeep.ui.format.suffix

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceDetailScreen(
    viewModel: MaintenanceDetailViewModel,
    onBack: () -> Unit,
    onRecordService: () -> Unit,
) {
    val dashboard by viewModel.dashboard.collectAsStateWithLifecycle()
    val forecast by viewModel.forecast.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    var showInterval by remember { mutableStateOf(false) }
    var showLastDone by remember { mutableStateOf(false) }

    val current = forecast
    val unit = dashboard?.vehicle?.mileageUnit
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(current?.item?.name ?: "Maintenance") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        if (current == null || unit == null) {
            Text("Loading…", modifier = Modifier.padding(innerPadding).padding(24.dp))
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        HealthDot(current.status)
                        Text(
                            text = current.status.label(),
                            color = current.status.color(),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Text(current.remainingLabel(unit), style = MaterialTheme.typography.titleMedium)
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Next service", style = MaterialTheme.typography.labelLarge)
                            Text(
                                text = current.nextServiceLabel(unit),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                "Whichever comes first.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    val interval = buildString {
                        append("Every ${formatMileage(current.item.intervalDistance, unit)}")
                        current.item.intervalMonths?.let { append(" or $it months") }
                    }
                    Text(interval, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    current.item.lastDoneMileage?.let { mileage ->
                        val date = current.item.lastDoneAtEpochDay?.let { " on ${formatEpochDay(it)}" }.orEmpty()
                        Text("Last done at ${formatMileage(mileage, unit)}$date")
                    }
                    Button(onClick = onRecordService, modifier = Modifier.fillMaxWidth()) {
                        Text("Record this service")
                    }
                    TextButton(onClick = { showInterval = true }) { Text("Adjust interval") }
                    if (current.status == HealthStatus.UNKNOWN) {
                        TextButton(onClick = { showLastDone = true }) { Text("Set last service") }
                    }
                }
            }
            if (history.isNotEmpty()) {
                item {
                    Text(
                        "History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                items(history, key = { it.id }) { visit ->
                    HistoryLine(
                        visit = visit,
                        unit = unit,
                        currencyCode = dashboard?.vehicle?.currencyCode,
                    )
                }
            }
        }
    }

    if (showInterval && forecast != null) {
        IntervalDialog(
            distance = forecast!!.item.intervalDistance.toString(),
            months = forecast!!.item.intervalMonths?.toString().orEmpty(),
            unitSuffix = unit?.suffix().orEmpty(),
            onDismiss = { showInterval = false },
            onConfirm = { distance, months ->
                viewModel.updateInterval(distance, months)
                showInterval = false
            },
        )
    }
    if (showLastDone) {
        LastDoneDialog(
            mileage = dashboard?.vehicle?.currentMileage?.toString().orEmpty(),
            unitSuffix = unit?.suffix().orEmpty(),
            onDismiss = { showLastDone = false },
            onConfirm = { mileage ->
                viewModel.setLastDone(mileage, Dates.todayEpochDay())
                showLastDone = false
            },
        )
    }
}

@Composable
private fun HistoryLine(
    visit: ServiceVisit,
    unit: MileageUnit,
    currencyCode: String?,
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = "${formatMileage(visit.mileage, unit)} — ${visit.itemNames.joinToString(" + ")}",
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = listOfNotNull(
                formatEpochDay(visit.performedAtEpochDay),
                visit.costMinor?.let { formatMoney(it, currencyCode) },
                visit.workshop,
            ).joinToString(" · "),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun IntervalDialog(
    distance: String,
    months: String,
    unitSuffix: String,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int?) -> Unit,
) {
    var distanceText by remember { mutableStateOf(distance) }
    var monthsText by remember { mutableStateOf(months) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Interval") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = distanceText,
                    onValueChange = { distanceText = it },
                    label = { Text("Distance") },
                    suffix = { Text(unitSuffix) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = monthsText,
                    onValueChange = { monthsText = it },
                    label = { Text("Months") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    distanceText.toIntOrNull()?.takeIf { it > 0 }?.let { onConfirm(it, monthsText.toIntOrNull()) }
                },
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun LastDoneDialog(
    mileage: String,
    unitSuffix: String,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    var text by remember { mutableStateOf(mileage) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Last service mileage") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                suffix = { Text(unitSuffix) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = { text.toIntOrNull()?.let(onConfirm) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
