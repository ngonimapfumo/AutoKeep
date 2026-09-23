package com.ngonim.autokeep.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ngonim.autokeep.ui.components.ServiceRow
import com.ngonim.autokeep.ui.components.StatusBadge
import com.ngonim.autokeep.ui.components.VehicleHero
import com.ngonim.autokeep.ui.format.formatMoney
import com.ngonim.autokeep.ui.format.formatShortDate
import com.ngonim.autokeep.ui.format.remainingLabelCompact
import com.ngonim.autokeep.ui.format.vehicleMeta

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onVehicle: (Long) -> Unit,
    onReminders: (Long) -> Unit,
    onItem: (Long, Long) -> Unit,
    onAddVehicle: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val title = if (state.greetingName.isBlank()) state.greeting else "${state.greeting}, ${state.greetingName}"
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            if (state.vehicle != null) {
                IconButton(onClick = { onReminders(state.vehicle!!.id) }) {
                    Icon(Icons.Outlined.Notifications, contentDescription = "Reminders")
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        val vehicle = state.vehicle
        if (vehicle == null) {
            Card(onClick = onAddVehicle, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Text("Add your first vehicle", fontWeight = FontWeight.SemiBold)
                    Text("Start a health dashboard and never miss a service.")
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onVehicle(vehicle.id) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
            ) {
                Column(Modifier.padding(16.dp)) {
                    VehicleHero(
                        modifier = Modifier.fillMaxWidth().height(140.dp),
                        photoUri = vehicle.photoUri,
                        contentDescription = vehicle.displayName,
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text(vehicle.displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(
                                vehicleMeta(vehicle.year, vehicle.powertrain, vehicle.transmission, vehicle.engineSize, vehicle.nickname),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        StatusBadge(state.badge)
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Stat(
                            label = "Next service",
                            value = state.nextService?.remainingLabelCompact(vehicle.mileageUnit) ?: "Not set",
                            modifier = Modifier.weight(1f),
                        )
                        Stat(
                            label = "Last service",
                            value = state.lastService?.let { formatShortDate(it.performedAtEpochDay) } ?: "—",
                            modifier = Modifier.weight(1f),
                        )
                        Stat(
                            label = "Total spend",
                            value = formatMoney(state.totalSpendMinor, vehicle.currencyCode),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Upcoming reminders", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                TextButton(onClick = { onReminders(vehicle.id) }) { Text("See all") }
            }
            if (state.reminders.isEmpty()) {
                Text("Nothing due. Your car is in good shape.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                state.reminders.forEach { forecast ->
                    ServiceRow(
                        forecast = forecast,
                        unit = vehicle.mileageUnit,
                        onClick = { onItem(vehicle.id, forecast.item.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
