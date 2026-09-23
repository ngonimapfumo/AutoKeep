package com.ngonim.autokeep.ui.onboarding

import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit,
    onAlreadyHaveAccount: () -> Unit,
) {
    val pager = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    Column(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(state = pager, modifier = Modifier.weight(1f)) { page ->
            if (page == 0) HeroPage() else FeaturesPage()
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(2) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (pager.currentPage == index) 18.dp else 8.dp, 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (pager.currentPage == index) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                },
                            ),
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            if (pager.currentPage == 0) {
                Button(
                    onClick = { scope.launch { pager.animateScrollToPage(1) } },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    shape = RoundedCornerShape(14.dp),
                ) { Text("Get Started", fontWeight = FontWeight.SemiBold) }
                TextButton(onClick = onAlreadyHaveAccount) {
                    Text("I already have an account", color = MaterialTheme.colorScheme.primary)
                }
            } else {
                Button(
                    onClick = onGetStarted,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    shape = RoundedCornerShape(14.dp),
                ) { Text("Next", fontWeight = FontWeight.SemiBold) }
            }
        }
    }
}

@Composable
private fun HeroPage() {
    val primary = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF1A1210), primary))),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            verticalArrangement = Arrangement.Bottom,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.DirectionsCar, contentDescription = null, tint = Color.White)
                Spacer(Modifier.size(8.dp))
                Text("AutoKeep", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            Text("Better care. Longer roads.", color = Color.White, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text(
                "Track your vehicle’s maintenance, keep your costs in check, and never miss a service again.",
                color = Color.White.copy(alpha = 0.82f),
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FeaturesPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(28.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.HealthAndSafety,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp),
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "Keep Your Vehicle in Top Shape.",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(20.dp))
        FeatureLine(Icons.Outlined.NotificationsActive, "Service Reminders")
        FeatureLine(Icons.Outlined.Payments, "Track Expenses")
        FeatureLine(Icons.Outlined.HealthAndSafety, "Vehicle Health")
    }
}

@Composable
private fun FeatureLine(icon: ImageVector, title: String) {
    Row(
        modifier = Modifier.padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
    }
}
