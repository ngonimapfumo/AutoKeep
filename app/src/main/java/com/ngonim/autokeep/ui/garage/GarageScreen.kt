package com.ngonim.autokeep.ui.garage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ngonim.autokeep.ui.components.StatusBadge
import com.ngonim.autokeep.ui.components.VehicleHero
import com.ngonim.autokeep.ui.format.formatMileage
import com.ngonim.autokeep.ui.format.vehicleMeta

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GarageScreen(
    viewModel: GarageViewModel,
    onVehicle: (Long) -> Unit,
    onAddVehicle: () -> Unit,
) {
    val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()
    var pendingDelete by remember { mutableStateOf<GarageVehicle?>(null) }
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("My Vehicles") },
            actions = {
                IconButton(onClick = onAddVehicle) {
                    Icon(Icons.Filled.Add, contentDescription = "Add vehicle")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        )
        if (vehicles.isEmpty()) {
            Text(
                "Add a vehicle to start tracking maintenance.",
                modifier = Modifier.padding(24.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(vehicles, key = { it.vehicle.id }) { row ->
                    var menuOpen by remember(row.vehicle.id) { mutableStateOf(false) }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onVehicle(row.vehicle.id) },
                        elevation = CardDefaults.cardElevation(2.dp),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            VehicleHero(
                                modifier = Modifier.fillMaxWidth().height(120.dp),
                                photoUri = row.vehicle.photoUri,
                                contentDescription = row.vehicle.displayName,
                            )
                            Spacer(Modifier.height(12.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(Modifier.weight(1f)) {
                                    Text(row.vehicle.displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Text(
                                        vehicleMeta(
                                            row.vehicle.year,
                                            row.vehicle.powertrain,
                                            row.vehicle.transmission,
                                            row.vehicle.engineSize,
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        formatMileage(row.vehicle.currentMileage, row.vehicle.mileageUnit),
                                        modifier = Modifier.padding(top = 4.dp),
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    IconButton(onClick = { menuOpen = true }) {
                                        Icon(Icons.Outlined.MoreVert, contentDescription = "Vehicle options")
                                    }
                                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                                        DropdownMenuItem(
                                            text = { Text("Remove from garage") },
                                            onClick = {
                                                menuOpen = false
                                                pendingDelete = row
                                            },
                                        )
                                    }
                                    StatusBadge(row.badge)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    pendingDelete?.let { row ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Remove ${row.vehicle.displayName}?") },
            text = { Text("This deletes the car, its maintenance checklist, and service history. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteVehicle(row.vehicle.id)
                        pendingDelete = null
                    },
                ) { Text("Remove") }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } },
        )
    }
}
