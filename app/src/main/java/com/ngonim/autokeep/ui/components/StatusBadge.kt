package com.ngonim.autokeep.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ngonim.autokeep.domain.model.VehicleHealthBadge
import com.ngonim.autokeep.ui.theme.DueAmber
import com.ngonim.autokeep.ui.theme.DueContainer
import com.ngonim.autokeep.ui.theme.OverdueContainer
import com.ngonim.autokeep.ui.theme.OverdueRed

@Composable
fun StatusBadge(badge: VehicleHealthBadge, modifier: Modifier = Modifier) {
    val (label, color, container) = when (badge) {
        VehicleHealthBadge.ALL_GOOD -> Triple(
            "All Good",
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer,
        )
        VehicleHealthBadge.DUE_SOON -> Triple("Due Soon", DueAmber, DueContainer)
        VehicleHealthBadge.OVERDUE -> Triple("Overdue", OverdueRed, OverdueContainer)
    }
    Surface(modifier = modifier, color = container, shape = RoundedCornerShape(20.dp)) {
        Text(
            text = label,
            color = color,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}
