package com.ngonim.autokeep.data

import android.content.Context
import com.ngonim.autokeep.ui.theme.AppColorScheme
import com.ngonim.autokeep.ui.theme.Appearance
import com.ngonim.autokeep.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class UserPreferences(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val _appearance = MutableStateFlow(readAppearance())
    val appearance: StateFlow<Appearance> = _appearance.asStateFlow()

    var displayName: String
        get() = prefs.getString(KEY_NAME, "") ?: ""
        set(value) {
            prefs.edit().putString(KEY_NAME, value).apply()
        }

    var email: String
        get() = prefs.getString(KEY_EMAIL, "") ?: ""
        set(value) {
            prefs.edit().putString(KEY_EMAIL, value).apply()
        }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME, mode.name).apply()
        _appearance.update { it.copy(themeMode = mode) }
    }

    fun setColorScheme(scheme: AppColorScheme) {
        prefs.edit().putString(KEY_SCHEME, scheme.name).apply()
        _appearance.update { it.copy(colorScheme = scheme) }
    }

    private fun readAppearance(): Appearance = Appearance(
        themeMode = runCatching {
            ThemeMode.valueOf(prefs.getString(KEY_THEME, ThemeMode.LIGHT.name)!!)
        }.getOrDefault(ThemeMode.LIGHT),
        colorScheme = runCatching {
            AppColorScheme.valueOf(prefs.getString(KEY_SCHEME, AppColorScheme.OCEAN.name)!!)
        }.getOrDefault(AppColorScheme.OCEAN),
    )

    companion object {
        private const val PREFS = "autokeep_user"
        private const val KEY_NAME = "display_name"
        private const val KEY_EMAIL = "email"
        private const val KEY_THEME = "theme_mode"
        private const val KEY_SCHEME = "color_scheme"
    }
}
