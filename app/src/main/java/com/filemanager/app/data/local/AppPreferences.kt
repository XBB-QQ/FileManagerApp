package com.filemanager.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

/**
 * Keys for app preferences stored in DataStore.
 */
object AppPreferenceKeys {
    val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
}

/**
 * App-wide preferences backed by DataStore.
 */
class AppPreferences @Singleton constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    /** Current dark mode setting as a Flow. */
    val isDarkMode: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[AppPreferenceKeys.DARK_MODE_KEY] ?: false }

    /** Persist dark mode preference. */
    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[AppPreferenceKeys.DARK_MODE_KEY] = enabled
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideAppPreferences(@ApplicationContext context: Context): AppPreferences {
        return AppPreferences(context)
    }
}
