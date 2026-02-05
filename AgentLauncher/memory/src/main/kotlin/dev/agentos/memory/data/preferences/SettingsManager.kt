package dev.agentos.memory.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "agent_settings"
)

/**
 * Manages application settings using DataStore.
 */
@Singleton
class SettingsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    // --- Keys ---
    private object Keys {
        val OPENAI_API_KEY = stringPreferencesKey("openai_api_key")
        val LLM_MODEL = stringPreferencesKey("llm_model")
        val VOICE_ENABLED = booleanPreferencesKey("voice_enabled")
        val TTS_ENABLED = booleanPreferencesKey("tts_enabled")
        val TTS_SPEED = floatPreferencesKey("tts_speed")
        val TTS_PITCH = floatPreferencesKey("tts_pitch")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val ANALYTICS_ENABLED = booleanPreferencesKey("analytics_enabled")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val LAST_SYNC_TIMESTAMP = longPreferencesKey("last_sync_timestamp")
    }

    // --- Settings Flow ---
    val settings: Flow<AgentSettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            AgentSettings(
                openAiApiKey = preferences[Keys.OPENAI_API_KEY],
                llmModel = preferences[Keys.LLM_MODEL] ?: "gpt-4o-mini",
                voiceEnabled = preferences[Keys.VOICE_ENABLED] ?: true,
                ttsEnabled = preferences[Keys.TTS_ENABLED] ?: true,
                ttsSpeed = preferences[Keys.TTS_SPEED] ?: 1.0f,
                ttsPitch = preferences[Keys.TTS_PITCH] ?: 1.0f,
                themeMode = ThemeMode.fromString(preferences[Keys.THEME_MODE]),
                hapticFeedback = preferences[Keys.HAPTIC_FEEDBACK] ?: true,
                analyticsEnabled = preferences[Keys.ANALYTICS_ENABLED] ?: false,
                onboardingCompleted = preferences[Keys.ONBOARDING_COMPLETED] ?: false,
                lastSyncTimestamp = preferences[Keys.LAST_SYNC_TIMESTAMP]
            )
        }

    // --- Individual Setters ---
    suspend fun setOpenAiApiKey(key: String?) {
        dataStore.edit { preferences ->
            if (key != null) {
                preferences[Keys.OPENAI_API_KEY] = key
            } else {
                preferences.remove(Keys.OPENAI_API_KEY)
            }
        }
    }

    suspend fun setLlmModel(model: String) {
        dataStore.edit { preferences ->
            preferences[Keys.LLM_MODEL] = model
        }
    }

    suspend fun setVoiceEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.VOICE_ENABLED] = enabled
        }
    }

    suspend fun setTtsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.TTS_ENABLED] = enabled
        }
    }

    suspend fun setTtsSpeed(speed: Float) {
        dataStore.edit { preferences ->
            preferences[Keys.TTS_SPEED] = speed.coerceIn(0.5f, 2.0f)
        }
    }

    suspend fun setTtsPitch(pitch: Float) {
        dataStore.edit { preferences ->
            preferences[Keys.TTS_PITCH] = pitch.coerceIn(0.5f, 2.0f)
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[Keys.THEME_MODE] = mode.name
        }
    }

    suspend fun setHapticFeedback(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.HAPTIC_FEEDBACK] = enabled
        }
    }

    suspend fun setAnalyticsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.ANALYTICS_ENABLED] = enabled
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun updateLastSyncTimestamp() {
        dataStore.edit { preferences ->
            preferences[Keys.LAST_SYNC_TIMESTAMP] = System.currentTimeMillis()
        }
    }

    suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }
}

/**
 * Application settings data class.
 */
data class AgentSettings(
    val openAiApiKey: String? = null,
    val llmModel: String = "gpt-4o-mini",
    val voiceEnabled: Boolean = true,
    val ttsEnabled: Boolean = true,
    val ttsSpeed: Float = 1.0f,
    val ttsPitch: Float = 1.0f,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val hapticFeedback: Boolean = true,
    val analyticsEnabled: Boolean = false,
    val onboardingCompleted: Boolean = false,
    val lastSyncTimestamp: Long? = null
)

/**
 * Theme mode options.
 */
enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM;

    companion object {
        fun fromString(value: String?): ThemeMode {
            return entries.find { it.name == value } ?: SYSTEM
        }
    }
}
