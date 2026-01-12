package com.example.byokvault.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.byokvault.KeyVaultApplication
import com.example.byokvault.data.model.PlatformWithKeys
import com.example.byokvault.data.repository.KeyVaultRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel для главного экрана
 */
class MainViewModel(
    private val repository: KeyVaultRepository
) : ViewModel() {
    
    /**
     * Состояние UI главного экрана
     */
    data class UiState(
        val platformsWithKeys: List<PlatformWithKeys> = emptyList(),
        val isLoading: Boolean = true,
        val error: String? = null
    )
    
    // StateFlow для всех платформ с ключами
    private val allPlatformsWithKeys = repository.getAllPlatformsWithKeys()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // UI состояние
    val uiState: StateFlow<UiState> = allPlatformsWithKeys
        .map { platforms ->
            UiState(
                platformsWithKeys = platforms.filter { it.apiKeys.isNotEmpty() },
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState(isLoading = true)
        )
    
    init {
        // Очистка пустых пользовательских платформ при запуске
        cleanupEmptyPlatforms()
    }
    
    /**
     * Удалить пустые пользовательские платформы
     */
    private fun cleanupEmptyPlatforms() {
        viewModelScope.launch {
            try {
                repository.cleanupEmptyCustomPlatforms()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    companion object {
        /**
         * Factory для создания ViewModel с зависимостями
         */
        fun provideFactory(
            repository: KeyVaultRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                return MainViewModel(repository) as T
            }
        }
    }
}
