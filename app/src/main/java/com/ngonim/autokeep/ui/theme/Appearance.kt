package com.ngonim.autokeep.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

enum class ThemeMode {
    LIGHT,
    DARK,
}

enum class AppColorScheme(
    val seed: Color,
    val label: String,
    val hex: String,
) {
    OCEAN(Color(0xFF3B78AF), "Ocean", "#3B78AF"),
    LIME(Color(0xFF9DCD69), "Lime", "#9DCD69"),
    BLUSH(Color(0xFFFFC0CB), "Blush", "#FFC0CB"),
    FOREST(Color(0xFF5D9F3D), "Forest", "#5D9F3D"),
    ROSE(Color(0xFFF78FA7), "Rose", "#F78FA7"),
}

data class Appearance(
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    val colorScheme: AppColorScheme = AppColorScheme.OCEAN,
)

fun Color.contrastingOnColor(): Color =
    if (luminance() > 0.55f) Color(0xFF1C1B1F) else Color.White
