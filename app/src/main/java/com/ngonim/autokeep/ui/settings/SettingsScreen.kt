package com.ngonim.autokeep.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ngonim.autokeep.data.UserPreferences
import com.ngonim.autokeep.ui.theme.AppColorScheme
import com.ngonim.autokeep.ui.theme.ThemeMode
import com.ngonim.autokeep.ui.theme.contrastingOnColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userPreferences: UserPreferences,
    onBack: () -> Unit,
) {
    val appearance by userPreferences.appearance.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        ) {
            Text("Theme", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                ThemeMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = appearance.themeMode == mode,
                        onClick = { userPreferences.setThemeMode(mode) },
                        shape = SegmentedButtonDefaults.itemShape(index, ThemeMode.entries.size),
                    ) {
                        Text(mode.name.lowercase().replaceFirstChar { it.titlecase() })
                    }
                }
            }
            Spacer(Modifier.height(28.dp))
            Text("Color scheme", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                AppColorScheme.entries.forEach { scheme ->
                    ColorSwatch(
                        scheme = scheme,
                        selected = appearance.colorScheme == scheme,
                        onClick = { userPreferences.setColorScheme(scheme) },
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "${appearance.colorScheme.label} · ${appearance.colorScheme.hex}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ColorSwatch(
    scheme: AppColorScheme,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(scheme.seed)
                .then(
                    if (selected) {
                        Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                    } else {
                        Modifier
                    },
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = scheme.label,
                    tint = scheme.seed.contrastingOnColor(),
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(scheme.label, style = MaterialTheme.typography.labelSmall)
    }
}
