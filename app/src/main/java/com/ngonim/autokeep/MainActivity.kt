package com.ngonim.autokeep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ngonim.autokeep.ui.AutoKeepApp
import com.ngonim.autokeep.ui.theme.AutoKeepTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as AutoKeepApplication
        setContent {
            val appearance by app.userPreferences.appearance.collectAsStateWithLifecycle()
            AutoKeepTheme(appearance = appearance) {
                AutoKeepApp(
                    repository = app.repository,
                    userPreferences = app.userPreferences,
                )
            }
        }
    }
}
