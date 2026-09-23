package com.ngonim.autokeep.ui.vehicle

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ngonim.autokeep.ui.components.ServiceRow
import com.ngonim.autokeep.ui.components.VehicleHero
import com.ngonim.autokeep.ui.components.rememberVehiclePhotoPicker
import com.ngonim.autokeep.ui.dashboard.DashboardViewModel
import com.ngonim.autokeep.ui.format.formatMileage
import com.ngonim.autokeep.ui.format.label
import com.ngonim.autokeep.ui.format.vehicleMeta

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleDetailsScreen(
    viewModel: DashboardViewModel,
    onBack: () -> Unit,
    onAddService: () -> Unit,
    onAddExpense: () -> Unit,
    onItem: (Long) -> Unit,
    onDeleted: () -> Unit,
) {
    val dashboard by viewModel.dashboard.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var menuOpen by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    val pickPhoto = rememberVehiclePhotoPicker { path -> viewModel.updatePhoto(path) }
    val vehicle = dashboard?.vehicle
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vehicle Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text(if (vehicle?.photoUri == null) "Add photo" else "Change photo") },
                            onClick = {
                                menuOpen = false
                                pickPhoto()
                            },
                        )
                        if (vehicle?.photoUri != null) {
                            DropdownMenuItem(
                                text = { Text("Remove photo") },
                                onClick = {
                                    menuOpen = false
                                    viewModel.updatePhoto(null)
                                },
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Remove from garage") },
                            onClick = {
                                menuOpen = false
                                confirmDelete = true
                            },
                        )
                    }
                },
            )
        },
    ) { inner ->
        if (vehicle == null) {
            Text("Loading…", modifier = Modifier.padding(inner).padding(24.dp))
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            VehicleHero(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clickable(onClick = pickPhoto),
                photoUri = vehicle.photoUri,
                contentDescription = vehicle.displayName,
                showAddHint = vehicle.photoUri == null,
            )
            Spacer(Modifier.height(12.dp))
            Text(vehicle.displayName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                vehicleMeta(vehicle.year, vehicle.powertrain, vehicle.transmission, vehicle.engineSize, vehicle.nickname),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            PrimaryTabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Overview") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Maintenance") })
                Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text("Info") })
            }
            Spacer(Modifier.height(16.dp))
            when (tab) {
                0 -> {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SpecCard(Icons.Outlined.LocalGasStation, "Fuel Type", vehicle.powertrain.label(), Modifier.weight(1f))
                        SpecCard(Icons.Outlined.Tune, "Engine Size", vehicle.engineSize ?: "—", Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SpecCard(Icons.Outlined.Settings, "Transmission", vehicle.transmission.label(), Modifier.weight(1f))
                        SpecCard(Icons.Outlined.Speed, "Mileage", formatMileage(vehicle.currentMileage, vehicle.mileageUnit), Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Quick actions", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Card(onClick = onAddService, modifier = Modifier.weight(1f)) {
                            Text("Add Service", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.SemiBold)
                        }
                        Card(onClick = onAddExpense, modifier = Modifier.weight(1f)) {
                            Text("Add Expense", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                1 -> dashboard?.items?.forEach { forecast ->
                    ServiceRow(forecast, vehicle.mileageUnit, onClick = { onItem(forecast.item.id) })
                }
                else -> {
                    InfoLine("Make", vehicle.make)
                    InfoLine("Model", vehicle.model)
                    InfoLine("Year", vehicle.year?.toString() ?: "—")
                    InfoLine("Nickname", vehicle.nickname ?: "—")
                }
            }
        }
    }
    if (confirmDelete && vehicle != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Remove ${vehicle.displayName}?") },
            text = { Text("This deletes the car, its maintenance checklist, and service history. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        viewModel.deleteVehicle(onDeleted)
                    },
                ) { Text("Remove") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun SpecCard(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.Start) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Column(Modifier.padding(vertical = 8.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}
