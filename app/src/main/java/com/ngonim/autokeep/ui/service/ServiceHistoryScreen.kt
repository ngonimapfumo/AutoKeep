package com.ngonim.autokeep.ui.service

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ngonim.autokeep.domain.model.MileageUnit
import com.ngonim.autokeep.ui.components.VehicleHero
import com.ngonim.autokeep.ui.format.formatMileage
import com.ngonim.autokeep.ui.format.formatMoney
import com.ngonim.autokeep.ui.format.formatShortDate
import com.ngonim.autokeep.ui.format.vehicleMeta

@Composable
fun ServiceHistoryScreen(
    viewModel: ServiceHistoryViewModel,
    embedded: Boolean = false,
) {
    val vehicle by viewModel.vehicle.collectAsStateWithLifecycle()
    val visits by viewModel.visits.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (!embedded) {
            item { Text("Service history", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
        }
        vehicle?.let { current ->
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        VehicleHero(
                            modifier = Modifier.size(64.dp),
                            photoUri = current.photoUri,
                            contentDescription = current.displayName,
                        )
                        Column(Modifier.padding(start = 12.dp).weight(1f)) {
                            Text(current.displayName, fontWeight = FontWeight.SemiBold)
                            Text(
                                vehicleMeta(current.year, current.powertrain, current.transmission, current.engineSize),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
        if (visits.isEmpty()) {
            item {
                Text("No visits yet. Record a service to start this car’s history.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        items(visits, key = { it.id }) { visit ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(20.dp)) {
                    Box(Modifier.size(10.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                    Box(Modifier.width(2.dp).fillMaxHeight().background(MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)))
                }
                Card(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(visit.itemNames.joinToString(" + ").ifBlank { "Service" }, fontWeight = FontWeight.SemiBold)
                        Text(
                            listOfNotNull(
                                formatShortDate(visit.performedAtEpochDay),
                                formatMileage(visit.mileage, vehicle?.mileageUnit ?: MileageUnit.KILOMETERS),
                                visit.costMinor?.let { formatMoney(it, vehicle?.currencyCode) },
                            ).joinToString(" · "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
