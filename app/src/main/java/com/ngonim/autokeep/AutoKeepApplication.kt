package com.ngonim.autokeep

import android.app.Application
import com.ngonim.autokeep.data.AutoKeepRepository
import com.ngonim.autokeep.data.UserPreferences
import com.ngonim.autokeep.data.local.AutoKeepDatabase

class AutoKeepApplication : Application() {
    lateinit var repository: AutoKeepRepository
        private set
    lateinit var userPreferences: UserPreferences
        private set

    override fun onCreate() {
        super.onCreate()
        repository = AutoKeepRepository(AutoKeepDatabase.create(this))
        userPreferences = UserPreferences(this)
    }
}
