package com.ngonim.autokeep.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun AutoKeepTheme(
    appearance: Appearance = Appearance(),
    content: @Composable () -> Unit,
) {
    val dark = when (appearance.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val seed = appearance.colorScheme.seed
    val onSeed = seed.contrastingOnColor()
    val colorScheme = if (dark) {
        darkColorScheme(
            primary = seed,
            onPrimary = onSeed,
            primaryContainer = seed.copy(alpha = 0.24f).compositeOver(Color(0xFF121212)),
            onPrimaryContainer = Color.White,
            secondary = seed,
            onSecondary = onSeed,
            tertiary = seed,
            background = Color(0xFF121212),
            surface = Color(0xFF1C1B1F),
            onBackground = Color(0xFFE6E1E5),
            onSurface = Color(0xFFE6E1E5),
            surfaceVariant = Color(0xFF2B2A2F),
            onSurfaceVariant = Color(0xFFCAC4D0),
            outline = Color(0xFF49454F),
            error = OverdueRed,
        )
    } else {
        lightColorScheme(
            primary = seed,
            onPrimary = onSeed,
            primaryContainer = seed.copy(alpha = 0.16f).compositeOver(Color.White),
            onPrimaryContainer = Color(0xFF1C1B1F),
            secondary = seed,
            onSecondary = onSeed,
            tertiary = seed,
            background = ScreenGray,
            surface = Color.White,
            onBackground = Color(0xFF1C1B1F),
            onSurface = Color(0xFF1C1B1F),
            surfaceVariant = Color(0xFFEDF1F5),
            onSurfaceVariant = Color(0xFF5B6776),
            outline = CardStroke,
            error = OverdueRed,
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !dark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
