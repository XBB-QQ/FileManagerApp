package com.filemanager.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.filemanager.app.data.local.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the settings screen.
 * Manages dark mode preference persistence and UI state.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appPreferences: AppPreferences
) : ViewModel() {

    /** Current dark mode state from DataStore. */
    val isDarkMode = appPreferences.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _darkModeChanged = MutableSharedFlow<Boolean>()
    val darkModeChanged = _darkModeChanged.asSharedFlow()

    /** Toggle dark mode and persist the change. */
    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            appPreferences.setDarkMode(enabled)
            _darkModeChanged.emit(enabled)
        }
    }
}
