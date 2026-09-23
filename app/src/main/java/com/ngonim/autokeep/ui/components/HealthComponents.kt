package com.ngonim.autokeep.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ngonim.autokeep.domain.model.HealthStatus
import com.ngonim.autokeep.domain.model.MileageUnit
import com.ngonim.autokeep.domain.model.ServiceForecast
import com.ngonim.autokeep.ui.format.label
import com.ngonim.autokeep.ui.format.remainingLabel
import com.ngonim.autokeep.ui.theme.DueAmber
import com.ngonim.autokeep.ui.theme.DueContainer
import com.ngonim.autokeep.ui.theme.GoodContainer
import com.ngonim.autokeep.ui.theme.GoodGreen
import com.ngonim.autokeep.ui.theme.OverdueContainer
import com.ngonim.autokeep.ui.theme.OverdueRed
import com.ngonim.autokeep.ui.theme.UnknownContainer
import com.ngonim.autokeep.ui.theme.UnknownGray

fun HealthStatus.color(): Color = when (this) {
    HealthStatus.GOOD -> GoodGreen
    HealthStatus.DUE_SOON -> DueAmber
    HealthStatus.OVERDUE -> OverdueRed
    HealthStatus.UNKNOWN -> UnknownGray
}

fun HealthStatus.containerColor(): Color = when (this) {
    HealthStatus.GOOD -> GoodContainer
    HealthStatus.DUE_SOON -> DueContainer
    HealthStatus.OVERDUE -> OverdueContainer
    HealthStatus.UNKNOWN -> UnknownContainer
}

@Composable
fun HealthDot(status: HealthStatus, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(status.color()),
    )
}

@Composable
fun HealthCountChip(status: HealthStatus, count: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = status.containerColor(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            HealthDot(status)
            Text(
                text = "$count ${status.label()}",
                style = MaterialTheme.typography.labelLarge,
                color = status.color(),
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
fun ServiceRow(
    forecast: ServiceForecast,
    unit: MileageUnit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HealthDot(forecast.status)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = forecast.item.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = forecast.remainingLabel(unit),
                style = MaterialTheme.typography.bodyMedium,
                color = forecast.status.color(),
            )
        }
    }
}
