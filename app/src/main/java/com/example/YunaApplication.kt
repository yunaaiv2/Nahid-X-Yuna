package com.example

import android.app.Application
import com.example.data.local.db.AppDatabase
import com.example.data.local.prefs.UserPreferencesDataStore
import com.example.data.repository.GeminiRepository
import com.example.phone.PhoneActionManager

class YunaApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var preferencesDataStore: UserPreferencesDataStore
        private set

    lateinit var phoneActionManager: PhoneActionManager
        private set

    lateinit var geminiRepository: GeminiRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = AppDatabase.getInstance(this)
        preferencesDataStore = UserPreferencesDataStore(this)
        phoneActionManager = PhoneActionManager(this)
        geminiRepository = GeminiRepository(this, preferencesDataStore, database.memoryDao())
    }

    companion object {
        lateinit var instance: YunaApplication
            private set
    }
}
