package com.example.byokvault.ui.screens.platformkeys

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.byokvault.data.model.APIKey
import com.example.byokvault.data.model.Platform
import com.example.byokvault.data.repository.KeyVaultRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана списка ключей платформы
 */
class PlatformKeysListViewModel(
    private val repository: KeyVaultRepository,
    private val platformId: Long
) : ViewModel() {
    
    /**
     * Состояние UI
     */
    data class UiState(
        val platform: Platform? = null,
        val apiKeys: List<APIKey> = emptyList(),
        val isLoading: Boolean = true
    )
    
    // Наблюдаем за платформой с её ключами
    private val platformWithKeys = repository.getPlatformWithKeys(platformId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    // UI состояние
    val uiState: StateFlow<UiState> = platformWithKeys
        .map { pwk ->
            UiState(
                platform = pwk?.platform,
                apiKeys = pwk?.apiKeys ?: emptyList(),
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState(isLoading = true)
        )
    
    companion object {
        fun provideFactory(
            repository: KeyVaultRepository,
            platformId: Long
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                return PlatformKeysListViewModel(repository, platformId) as T
            }
        }
    }
}
