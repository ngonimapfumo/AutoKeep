package com.ngonim.autokeep.ui.reminders

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ngonim.autokeep.domain.model.HealthStatus
import com.ngonim.autokeep.ui.components.ServiceRow
import com.ngonim.autokeep.ui.dashboard.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    viewModel: DashboardViewModel,
    onBack: () -> Unit,
    onItem: (Long) -> Unit,
) {
    val dashboard by viewModel.dashboard.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reminders") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { inner ->
        val current = dashboard
        if (current == null) {
            Text("Loading…", modifier = Modifier.padding(inner).padding(24.dp))
            return@Scaffold
        }
        Column(Modifier.fillMaxSize().padding(inner)) {
            PrimaryTabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Upcoming") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Completed") })
            }
            val rows = if (tab == 0) {
                current.items.filter { it.status != HealthStatus.GOOD }
            } else {
                current.items.filter { it.status == HealthStatus.GOOD && it.item.lastDoneMileage != null }
            }
            if (rows.isEmpty()) {
                Text(
                    if (tab == 0) "Nothing upcoming." else "No completed services yet.",
                    modifier = Modifier.padding(24.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyColumn(modifier = Modifier.padding(horizontal = 20.dp)) {
                    items(rows, key = { it.item.id }) { forecast ->
                        ServiceRow(forecast, current.vehicle.mileageUnit, onClick = { onItem(forecast.item.id) })
                    }
                }
            }
        }
    }
}
