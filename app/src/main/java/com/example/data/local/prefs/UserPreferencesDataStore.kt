package com.example.data.local.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "yuna_preferences")

data class YunaSettings(
    val language: String = "auto", // "auto", "bn", "ko", "en"
    val personality: String = "Friendly", // "Friendly", "Calm", "Playful", "Concise", "Professional"
    val voiceSpeed: Float = 1.0f,
    val ttsEnabled: Boolean = true,
    val masterMemoryEnabled: Boolean = true,
    val floatingOverlayEnabled: Boolean = false,
    val customApiKey: String = "",
    val reducedMotion: Boolean = false
)

class UserPreferencesDataStore(private val context: Context) {

    private object PreferencesKeys {
        val LANGUAGE = stringPreferencesKey("yuna_language")
        val PERSONALITY = stringPreferencesKey("yuna_personality")
        val VOICE_SPEED = floatPreferencesKey("yuna_voice_speed")
        val TTS_ENABLED = booleanPreferencesKey("yuna_tts_enabled")
        val MASTER_MEMORY_ENABLED = booleanPreferencesKey("yuna_master_memory_enabled")
        val FLOATING_OVERLAY_ENABLED = booleanPreferencesKey("yuna_floating_overlay_enabled")
        val CUSTOM_API_KEY = stringPreferencesKey("yuna_custom_api_key")
        val REDUCED_MOTION = booleanPreferencesKey("yuna_reduced_motion")
    }

    val settingsFlow: Flow<YunaSettings> = context.dataStore.data.map { prefs ->
        YunaSettings(
            language = prefs[PreferencesKeys.LANGUAGE] ?: "auto",
            personality = prefs[PreferencesKeys.PERSONALITY] ?: "Friendly",
            voiceSpeed = prefs[PreferencesKeys.VOICE_SPEED] ?: 1.0f,
            ttsEnabled = prefs[PreferencesKeys.TTS_ENABLED] ?: true,
            masterMemoryEnabled = prefs[PreferencesKeys.MASTER_MEMORY_ENABLED] ?: true,
            floatingOverlayEnabled = prefs[PreferencesKeys.FLOATING_OVERLAY_ENABLED] ?: false,
            customApiKey = prefs[PreferencesKeys.CUSTOM_API_KEY] ?: "",
            reducedMotion = prefs[PreferencesKeys.REDUCED_MOTION] ?: false
        )
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { it[PreferencesKeys.LANGUAGE] = language }
    }

    suspend fun setPersonality(personality: String) {
        context.dataStore.edit { it[PreferencesKeys.PERSONALITY] = personality }
    }

    suspend fun setVoiceSpeed(speed: Float) {
        context.dataStore.edit { it[PreferencesKeys.VOICE_SPEED] = speed }
    }

    suspend fun setTtsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.TTS_ENABLED] = enabled }
    }

    suspend fun setMasterMemoryEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.MASTER_MEMORY_ENABLED] = enabled }
    }

    suspend fun setFloatingOverlayEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.FLOATING_OVERLAY_ENABLED] = enabled }
    }

    suspend fun setCustomApiKey(key: String) {
        context.dataStore.edit { it[PreferencesKeys.CUSTOM_API_KEY] = key }
    }

    suspend fun setReducedMotion(reduced: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.REDUCED_MOTION] = reduced }
    }
}
