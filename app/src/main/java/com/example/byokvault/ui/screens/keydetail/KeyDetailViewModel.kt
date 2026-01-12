package com.example.byokvault.ui.screens.keydetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.byokvault.data.keystore.KeystoreService
import com.example.byokvault.data.model.APIKey
import com.example.byokvault.data.model.Platform
import com.example.byokvault.data.repository.KeyVaultRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана деталей API ключа
 */
class KeyDetailViewModel(
    private val repository: KeyVaultRepository,
    private val keystoreService: KeystoreService,
    private val keyId: Long
) : ViewModel() {
    
    /**
     * Состояние UI
     */
    data class UiState(
        val apiKey: APIKey? = null,
        val platform: Platform? = null,
        val keyValue: String = "",
        val isLoading: Boolean = true,
        val showCopiedMessage: Boolean = false,
        val showDeleteDialog: Boolean = false
    )
    
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    init {
        observeKeyDetails()
    }
    
    /**
     * Наблюдать за деталями ключа
     */
    private fun observeKeyDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val key = repository.getKeyById(keyId)
                if (key == null) {
                    _uiState.update { it.copy(isLoading = false) }
                    return@launch
                }

                repository.getPlatformWithKeys(key.platformId)
                    .filterNotNull()
                    .map { platformWithKeys ->
                        val apiKey = platformWithKeys.apiKeys.first { it.id == keyId }
                        val keyValue = keystoreService.get(apiKey.keystoreId) ?: ""
                        Triple(apiKey, platformWithKeys.platform, keyValue)
                    }
                    .collect { (apiKey, platform, keyValue) ->
                        _uiState.update {
                            it.copy(
                                apiKey = apiKey,
                                platform = platform,
                                keyValue = keyValue,
                                isLoading = false
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    /**
     * Показать сообщение "Скопировано"
     */
    fun showCopiedMessage() {
        _uiState.update { it.copy(showCopiedMessage = true) }
        
        // Скрыть через 2 секунды
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            _uiState.update { it.copy(showCopiedMessage = false) }
        }
    }
    
    /**
     * Показать диалог удаления
     */
    fun showDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }
    
    /**
     * Скрыть диалог удаления
     */
    fun dismissDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }
    
    /**
     * Удалить ключ
     */
    fun deleteKey(onDeleted: () -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            val key = state.apiKey ?: return@launch
            val platform = state.platform
            
            try {
                // Проверяем, последний ли это ключ платформы
                val keyCount = repository.getKeyCountForPlatform(key.platformId)
                val isLastKey = keyCount == 1
                
                // Удаляем из Keystore
                keystoreService.delete(key.keystoreId)
                
                // Удаляем из базы данных
                repository.deleteKey(key)
                
                // Если это был последний ключ и платформа не дефолтная - удаляем платформу
                if (isLastKey && platform != null && !platform.isDefault) {
                    repository.deletePlatform(platform)
                }
                
                onDeleted()
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    companion object {
        fun provideFactory(
            repository: KeyVaultRepository,
            keystoreService: KeystoreService,
            keyId: Long
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                return KeyDetailViewModel(repository, keystoreService, keyId) as T
            }
        }
    }
}
