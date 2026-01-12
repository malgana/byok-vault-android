package com.example.byokvault.ui.screens.addkey

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.byokvault.data.keystore.KeystoreService
import com.example.byokvault.data.model.APIKey
import com.example.byokvault.data.model.Platform
import com.example.byokvault.data.repository.KeyVaultRepository
import com.example.byokvault.utils.ImageHelper
import com.example.byokvault.utils.KeyValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel для экрана добавления/редактирования API ключа
 */
class AddKeyViewModel(
    private val repository: KeyVaultRepository,
    private val keystoreService: KeystoreService,
    private val keyValidator: KeyValidator,
    private val editingKeyId: Long?,
    private val preselectedPlatformId: Long?
) : ViewModel() {
    
    /**
     * Состояние UI
     */
    data class UiState(
        // Основные поля
        val myName: String = "",
        val apiKeyValue: String = "",
        val note: String = "",
        val selectedPlatformName: String = "",
        val customPlatformName: String = "",
        val customIconData: String? = null,
        
        // Состояния
        val isEditMode: Boolean = false,
        val isLoading: Boolean = false,
        val isSaving: Boolean = false,
        val isValidating: Boolean = false,
        val validationSuccess: Boolean = false,
        
        // Данные
        val availablePlatforms: List<String> = emptyList(),
        val editingKey: APIKey? = null,
        val editingPlatform: Platform? = null,
        
        // Ошибки
        val errorMessage: String? = null,
        val showError: Boolean = false
    )
    
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    init {
        loadInitialData()
    }
    
    /**
     * Загрузить начальные данные
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Загружаем список платформ
                val platforms = repository.getAllPlatformsWithKeys()
                var platformNames = mutableListOf<String>()
                
                platforms.collect { list ->
                    val existingNames = list.map { it.platform.name }
                    val allNames = (existingNames + Platform.defaultPlatforms).toSet().sorted()
                    platformNames = (allNames + "New").toMutableList()
                }
                
                // Если редактируем существующий ключ
                if (editingKeyId != null) {
                    val key = repository.getKeyById(editingKeyId)
                    if (key != null) {
                        val keyValue = keystoreService.get(key.keystoreId) ?: ""
                        val platformWithKeys = repository.getPlatformWithKeys(key.platformId)
                        
                        var platform: Platform? = null
                        platformWithKeys.collect { pwk ->
                            platform = pwk?.platform
                        }
                        
                        _uiState.update { state ->
                            state.copy(
                                isEditMode = true,
                                myName = key.myName,
                                apiKeyValue = keyValue,
                                note = key.note ?: "",
                                selectedPlatformName = platform?.name ?: "",
                                editingKey = key,
                                editingPlatform = platform,
                                availablePlatforms = platformNames,
                                isLoading = false
                            )
                        }
                        return@launch
                    }
                }
                
                // Если есть предвыбранная платформа
                if (preselectedPlatformId != null) {
                    val platformWithKeys = repository.getPlatformWithKeys(preselectedPlatformId)
                    platformWithKeys.collect { pwk ->
                        _uiState.update { state ->
                            state.copy(
                                selectedPlatformName = pwk?.platform?.name ?: "",
                                availablePlatforms = platformNames,
                                isLoading = false
                            )
                        }
                    }
                } else {
                    _uiState.update { state ->
                        state.copy(
                            availablePlatforms = platformNames,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                showError("Ошибка загрузки данных: ${e.message}")
            }
        }
    }
    
    /**
     * Обновить название ключа
     */
    fun updateMyName(name: String) {
        _uiState.update { it.copy(myName = name) }
    }
    
    /**
     * Обновить значение API ключа
     */
    fun updateApiKeyValue(value: String) {
        _uiState.update { it.copy(apiKeyValue = value) }
    }
    
    /**
     * Обновить заметку
     */
    fun updateNote(note: String) {
        _uiState.update { it.copy(note = note) }
    }
    
    /**
     * Выбрать платформу
     */
    fun selectPlatform(platformName: String) {
        _uiState.update { it.copy(selectedPlatformName = platformName) }
    }
    
    /**
     * Обновить название кастомной платформы
     */
    fun updateCustomPlatformName(name: String) {
        _uiState.update { it.copy(customPlatformName = name) }
    }
    
    /**
     * Загрузить иконку из Uri
     */
    fun loadIcon(context: Context, uri: Uri) {
        viewModelScope.launch {
            val iconData = ImageHelper.processImage(context, uri)
            _uiState.update { it.copy(customIconData = iconData) }
        }
    }
    
    /**
     * Вставить ключ из буфера обмена
     */
    fun pasteFromClipboard(clipboardText: String) {
        _uiState.update { it.copy(apiKeyValue = clipboardText) }
    }
    
    /**
     * Проверить и сохранить ключ
     */
    fun validateAndSave(onSuccess: () -> Unit) {
        val state = _uiState.value
        
        // Валидация полей
        if (state.myName.isBlank()) {
            showError("Введите название ключа")
            return
        }
        
        if (state.apiKeyValue.isBlank()) {
            showError("Введите значение ключа")
            return
        }
        
        val finalPlatformName = if (state.selectedPlatformName == "New") {
            state.customPlatformName.trim()
        } else {
            state.selectedPlatformName
        }
        
        if (finalPlatformName.isBlank()) {
            showError("Выберите или введите название платформы")
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            try {
                if (state.isEditMode) {
                    updateExistingKey(onSuccess)
                } else {
                    // Проверка на дубликат
                    val duplicateCheck = keyValidator.checkForDuplicate(
                        keyValue = state.apiKeyValue
                    )
                    
                    when (duplicateCheck) {
                        is KeyValidator.DuplicateCheckResult.Duplicate -> {
                            _uiState.update { it.copy(isSaving = false, apiKeyValue = "") }
                            showError(
                                "Этот ключ уже добавлен: \"${duplicateCheck.existingKey.myName}\" " +
                                "(${duplicateCheck.platformName})"
                            )
                        }
                        is KeyValidator.DuplicateCheckResult.NotDuplicate -> {
                            createNewKey(finalPlatformName, onSuccess)
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false) }
                showError("Ошибка сохранения: ${e.message}")
            }
        }
    }
    
    /**
     * Создать новый ключ
     */
    private suspend fun createNewKey(platformName: String, onSuccess: () -> Unit) {
        val state = _uiState.value
        
        try {
            // Найти или создать платформу
            val platform = repository.getOrCreatePlatform(
                name = platformName,
                customIconData = state.customIconData
            )
            
            // Создать новый ключ
            val keystoreId = UUID.randomUUID().toString()
            val noteValue = state.note.trim().ifBlank { null }
            
            val newKey = APIKey(
                myName = state.myName,
                keystoreId = keystoreId,
                platformId = platform.id,
                note = noteValue,
                isValid = false
            )
            
            // Сохранить значение ключа в Keystore
            val saved = keystoreService.save(state.apiKeyValue, keystoreId)
            if (!saved) {
                showError("Не удалось сохранить ключ в Keystore")
                _uiState.update { it.copy(isSaving = false) }
                return
            }
            
            // Сохранить в базу данных
            repository.insertKey(newKey)
            
            _uiState.update { it.copy(isSaving = false) }
            onSuccess()
            
        } catch (e: Exception) {
            _uiState.update { it.copy(isSaving = false) }
            showError("Ошибка создания ключа: ${e.message}")
        }
    }
    
    /**
     * Обновить существующий ключ
     */
    private suspend fun updateExistingKey(onSuccess: () -> Unit) {
        val state = _uiState.value
        val editingKey = state.editingKey ?: return
        
        try {
            // Обновляем метаданные
            val noteValue = state.note.trim().ifBlank { null }
            val updatedKey = editingKey.copy(
                myName = state.myName,
                note = noteValue
            )
            
            // Проверяем, изменился ли сам ключ
            val currentKeyValue = keystoreService.get(editingKey.keystoreId) ?: ""
            if (currentKeyValue != state.apiKeyValue) {
                // Ключ изменился - обновляем в Keystore и сбрасываем валидацию
                val updated = keystoreService.update(state.apiKeyValue, editingKey.keystoreId)
                if (!updated) {
                    showError("Не удалось обновить ключ в Keystore")
                    _uiState.update { it.copy(isSaving = false) }
                    return
                }
                
                repository.updateKey(updatedKey.copy(isValid = false))
            } else {
                repository.updateKey(updatedKey)
            }
            
            _uiState.update { it.copy(isSaving = false) }
            onSuccess()
            
        } catch (e: Exception) {
            _uiState.update { it.copy(isSaving = false) }
            showError("Ошибка обновления ключа: ${e.message}")
        }
    }
    
    /**
     * Показать ошибку
     */
    private fun showError(message: String) {
        _uiState.update { 
            it.copy(
                errorMessage = message,
                showError = true
            )
        }
    }
    
    /**
     * Скрыть ошибку
     */
    fun dismissError() {
        _uiState.update { it.copy(showError = false) }
    }
    
    /**
     * Проверить валидность формы
     */
    fun isFormValid(): Boolean {
        val state = _uiState.value
        val platformValid = state.selectedPlatformName.isNotBlank() || 
                          (state.selectedPlatformName == "New" && state.customPlatformName.isNotBlank())
        
        return state.myName.isNotBlank() && 
               state.apiKeyValue.isNotBlank() && 
               platformValid
    }
    
    companion object {
        fun provideFactory(
            repository: KeyVaultRepository,
            keystoreService: KeystoreService,
            editingKeyId: Long?,
            preselectedPlatformId: Long?
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val keyValidator = KeyValidator(repository, keystoreService)
                return AddKeyViewModel(
                    repository,
                    keystoreService,
                    keyValidator,
                    editingKeyId,
                    preselectedPlatformId
                ) as T
            }
        }
    }
}
