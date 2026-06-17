package com.filemanager.app.presentation.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.filemanager.app.domain.model.StorageInfo
import com.filemanager.app.domain.repository.FileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the home screen.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val fileRepository: FileRepository
) : AndroidViewModel(application) {

    private val _storages = MutableStateFlow<List<StorageInfo>>(emptyList())
    val storages: StateFlow<List<StorageInfo>> = _storages.asStateFlow()

    init {
        loadStorageInfo()
    }

    private fun loadStorageInfo() {
        viewModelScope.launch {
            fileRepository.getStorageInfo().collect { result ->
                result.onSuccess { storages ->
                    _storages.value = storages
                }
            }
        }
    }
}
