package com.ngonim.autokeep.ui.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ngonim.autokeep.data.UserPreferences

@Composable
fun MoreScreen(
    userPreferences: UserPreferences,
    onVehicles: () -> Unit,
    onReminders: () -> Unit,
    onHistory: () -> Unit,
    onExpenses: () -> Unit,
    onSettings: () -> Unit,
) {
    var name by remember { mutableStateOf(userPreferences.displayName) }
    var email by remember { mutableStateOf(userPreferences.email) }
    var edit by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Row(
            modifier = Modifier.clickable { edit = true }.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(56.dp)) {
                Icon(Icons.Outlined.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(12.dp))
            }
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(name.ifBlank { "Your name" }, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(email.ifBlank { "Add your email" }, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(24.dp))
        MenuRow(Icons.Outlined.DirectionsCar, "My Vehicles", onVehicles)
        MenuRow(Icons.Outlined.Notifications, "Reminders", onReminders)
        MenuRow(Icons.Outlined.History, "Service History", onHistory)
        MenuRow(Icons.Outlined.Payments, "Expenses", onExpenses)
        MenuRow(Icons.Outlined.Settings, "Settings", onSettings)
        MenuRow(Icons.AutoMirrored.Outlined.HelpOutline, "Help & Support") { }
        Spacer(Modifier.weight(1f))
        Text("AutoKeep 1.0", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }

    if (edit) {
        AlertDialog(
            onDismissRequest = { edit = false },
            title = { Text("Profile") },
            text = {
                Column {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, singleLine = true)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        userPreferences.displayName = name
                        userPreferences.email = email
                        edit = false
                    },
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { edit = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun MenuRow(icon: ImageVector, title: String, onClick: () -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(title, modifier = Modifier.padding(start = 16.dp), style = MaterialTheme.typography.titleMedium)
        }
        HorizontalDivider()
    }
}
