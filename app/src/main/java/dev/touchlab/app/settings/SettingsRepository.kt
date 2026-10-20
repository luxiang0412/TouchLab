package dev.touchlab.app.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

data class Settings(
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val reduceMotion: Boolean = false,
)

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "touchlab_settings",
)

class SettingsRepository(context: Context) {
    private val appContext = context.applicationContext
    private val store = appContext.settingsDataStore

    val settings: Flow<Settings> = store.data
        .catch { throwable ->
            if (throwable is IOException) {
                emit(emptyPreferences())
            } else {
                throw throwable
            }
        }
        .map { preferences ->
            Settings(
                soundEnabled = preferences[SOUND] ?: true,
                hapticsEnabled = preferences[HAPTIC] ?: true,
                reduceMotion = preferences[REDUCE_MOTION] ?: false,
            )
        }

    suspend fun setSoundEnabled(enabled: Boolean) {
        store.edit { it[SOUND] = enabled }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        store.edit { it[HAPTIC] = enabled }
    }

    suspend fun setReduceMotion(enabled: Boolean) {
        store.edit { it[REDUCE_MOTION] = enabled }
    }

    private companion object {
        val SOUND = booleanPreferencesKey("sound_enabled")
        val HAPTIC = booleanPreferencesKey("haptics_enabled")
        val REDUCE_MOTION = booleanPreferencesKey("reduce_motion")
    }
}
