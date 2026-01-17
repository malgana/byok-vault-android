package com.example.byokvault.ui.screens.platformkeys

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.byokvault.data.keystore.KeystoreService
import com.example.byokvault.data.model.APIKey
import com.example.byokvault.data.model.Platform
import com.example.byokvault.data.repository.KeyVaultRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана списка ключей платформы
 * Обновлено для соответствия iOS версии с копированием
 */
class PlatformKeysListViewModel(
    private val repository: KeyVaultRepository,
    private val keystoreService: KeystoreService,
    private val platformId: Long
) : ViewModel() {
    
    /**
     * Состояние UI
     */
    data class UiState(
        val platform: Platform? = null,
        val apiKeys: List<APIKey> = emptyList(),
        val isLoading: Boolean = true,
        val copiedKeyId: String? = null // keystoreId скопированного ключа
    )
    
    // Состояние копирования
    private val _copiedKeyId = MutableStateFlow<String?>(null)
    
    // Наблюдаем за платформой с её ключами
    private val platformWithKeys = repository.getPlatformWithKeys(platformId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    // UI состояние
    val uiState: StateFlow<UiState> = combine(
        platformWithKeys,
        _copiedKeyId
    ) { pwk, copiedId ->
        UiState(
            platform = pwk?.platform,
            apiKeys = pwk?.apiKeys ?: emptyList(),
            isLoading = pwk == null,
            copiedKeyId = copiedId
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState(isLoading = true)
    )
    
    /**
     * Получить значение ключа для копирования
     */
    fun getKeyValue(keystoreId: String): String? {
        return keystoreService.get(keystoreId)
    }
    
    /**
     * Показать сообщение о копировании
     */
    fun showCopiedMessage(keystoreId: String) {
        viewModelScope.launch {
            _copiedKeyId.value = keystoreId
            
            // Сбрасываем через 2 секунды
            delay(2000)
            
            if (_copiedKeyId.value == keystoreId) {
                _copiedKeyId.value = null
            }
        }
    }
    
    companion object {
        fun provideFactory(
            repository: KeyVaultRepository,
            keystoreService: KeystoreService,
            platformId: Long
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                return PlatformKeysListViewModel(repository, keystoreService, platformId) as T
            }
        }
    }
}
