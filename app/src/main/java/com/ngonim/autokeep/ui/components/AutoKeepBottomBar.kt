package com.ngonim.autokeep.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ngonim.autokeep.ui.navigation.MainTab

@Composable
fun AutoKeepBottomBar(
    selected: MainTab,
    onTab: (MainTab) -> Unit,
    onAdd: () -> Unit,
) {
    Box {
        NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
            NavigationBarItem(
                selected = selected == MainTab.HOME,
                onClick = { onTab(MainTab.HOME) },
                icon = { Icon(Icons.Outlined.Home, contentDescription = "Home") },
                label = { Text("Home") },
                colors = navColors(),
            )
            NavigationBarItem(
                selected = selected == MainTab.VEHICLES,
                onClick = { onTab(MainTab.VEHICLES) },
                icon = { Icon(Icons.Outlined.DirectionsCar, contentDescription = "Vehicles") },
                label = { Text("Vehicles") },
                colors = navColors(),
            )
            NavigationBarItem(
                selected = false,
                onClick = onAdd,
                icon = { Box(Modifier.size(24.dp)) },
                label = { Text("") },
                enabled = true,
            )
            NavigationBarItem(
                selected = selected == MainTab.HISTORY,
                onClick = { onTab(MainTab.HISTORY) },
                icon = { Icon(Icons.Outlined.History, contentDescription = "History") },
                label = { Text("History") },
                colors = navColors(),
            )
            NavigationBarItem(
                selected = selected == MainTab.MORE,
                onClick = { onTab(MainTab.MORE) },
                icon = { Icon(Icons.Outlined.MoreHoriz, contentDescription = "More") },
                label = { Text("More") },
                colors = navColors(),
            )
        }
        FloatingActionButton(
            onClick = onAdd,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-18).dp)
                .size(58.dp),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add")
        }
    }
}

@Composable
private fun navColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = MaterialTheme.colorScheme.primary,
    selectedTextColor = MaterialTheme.colorScheme.primary,
    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
)
