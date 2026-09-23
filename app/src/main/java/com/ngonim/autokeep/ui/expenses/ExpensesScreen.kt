package com.ngonim.autokeep.ui.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.ngonim.autokeep.domain.model.ExpenseFilter
import com.ngonim.autokeep.ui.format.formatMoney
import com.ngonim.autokeep.ui.format.formatShortDate
import com.ngonim.autokeep.ui.format.label
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    viewModel: ExpensesViewModel,
    onBack: () -> Unit,
    onAddExpense: () -> Unit,
) {
    val overview by viewModel.overview.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val vehicle by viewModel.vehicle.collectAsStateWithLifecycle()
    val currency = vehicle?.currencyCode

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expenses") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        bottomBar = {
            Button(
                onClick = onAddExpense,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            ) { Text("Add Expense") }
        },
    ) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExpenseFilter.entries.forEach { option ->
                        FilterChip(
                            selected = filter == option,
                            onClick = { viewModel.setFilter(option) },
                            label = { Text(option.name.lowercase().replaceFirstChar { it.titlecase() }) },
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("Total spent (${overview.year})", style = MaterialTheme.typography.labelLarge)
                Text(formatMoney(overview.totalMinor, currency), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                val change = percentChange(overview.totalMinor, overview.previousYearTotalMinor)
                if (change != null) {
                    Text("$change vs last year", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(12.dp))
                MonthlyChart(overview.monthly)
            }
            items(overview.entries, key = { it.id }) { entry ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(entry.title, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${entry.category.label()} · ${formatShortDate(entry.performedAtEpochDay)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(formatMoney(entry.costMinor, currency), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthlyChart(monthly: List<Long>) {
    val max = monthly.maxOrNull()?.coerceAtLeast(1L) ?: 1L
    val labels = listOf("J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D")
    Row(
        modifier = Modifier.fillMaxWidth().height(140.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        monthly.forEachIndexed { index, value ->
            val fraction = (value.toFloat() / max.toFloat()).coerceIn(0.04f, 1f)
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(fraction)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primary),
                )
                Text(labels[index], style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

private fun percentChange(current: Long, previous: Long): String? {
    if (previous <= 0) return null
    val percent = (((current - previous).toDouble() / previous) * 100).roundToInt()
    val sign = if (percent >= 0) "+" else ""
    return "$sign$percent%"
}
