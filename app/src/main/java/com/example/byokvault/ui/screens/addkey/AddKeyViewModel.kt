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
import com.example.byokvault.data.validation.KeyValidationService
import com.example.byokvault.data.validation.ValidationResult
import com.example.byokvault.utils.ImageHelper
import com.example.byokvault.utils.KeyValidator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel для экрана добавления/редактирования API ключа
 * Обновлено для соответствия iOS версии с валидацией
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
        val validationFailed: Boolean = false,
        
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
                val platformsList = repository.getAllPlatformsWithKeys().first()
                val existingNames = platformsList.map { it.platform.name }
                val platformNames = (existingNames + Platform.defaultPlatforms).toSet().sorted() + "New"

                // Если редактируем существующий ключ
                if (editingKeyId != null) {
                    val key = repository.getKeyById(editingKeyId)
                    if (key != null) {
                        val keyValue = keystoreService.get(key.keystoreId) ?: ""
                        val platform = repository.getPlatformWithKeys(key.platformId).firstOrNull()?.platform
                        
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
                    val platform = repository.getPlatformWithKeys(preselectedPlatformId).firstOrNull()?.platform
                    _uiState.update { state ->
                        state.copy(
                            selectedPlatformName = platform?.name ?: "",
                            availablePlatforms = platformNames,
                            isLoading = false
                        )
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
     * Получить финальное имя платформы
     */
    private fun getFinalPlatformName(): String {
        val state = _uiState.value
        return if (state.selectedPlatformName == "New") {
            state.customPlatformName.trim()
        } else {
            state.selectedPlatformName
        }
    }
    
    /**
     * Проверить, поддерживает ли платформа валидацию
     */
    fun supportsValidation(): Boolean {
        return KeyValidationService.supportsValidation(getFinalPlatformName())
    }
    
    /**
     * Получить текст кнопки
     */
    fun getButtonText(): String {
        val state = _uiState.value
        return when {
            state.isEditMode -> "Сохранить изменения"
            state.validationSuccess -> "Ключ работает"
            state.validationFailed -> "Сохранить ключ"
            supportsValidation() -> "Проверить"
            else -> "Сохранить ключ"
        }
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
        
        val finalPlatformName = getFinalPlatformName()
        
        if (finalPlatformName.isBlank()) {
            showError("Выберите или введите название платформы")
            return
        }
        
        viewModelScope.launch {
            // Режим редактирования - просто сохраняем
            if (state.isEditMode) {
                _uiState.update { it.copy(isSaving = true) }
                updateExistingKey(finalPlatformName, onSuccess)
                return@launch
            }
            
            // Проверка на дубликат
            val duplicateCheck = keyValidator.checkForDuplicate(keyValue = state.apiKeyValue)
            
            when (duplicateCheck) {
                is KeyValidator.DuplicateCheckResult.Duplicate -> {
                    _uiState.update { it.copy(apiKeyValue = "") }
                    showError(
                        "Этот ключ уже добавлен: \"${duplicateCheck.existingKey.myName}\" " +
                        "(${duplicateCheck.platformName})"
                    )
                    return@launch
                }
                is KeyValidator.DuplicateCheckResult.NotDuplicate -> {
                    // Продолжаем
                }
            }
            
            // Если валидация уже не прошла ранее - сохраняем без валидации
            if (state.validationFailed) {
                _uiState.update { it.copy(isSaving = true) }
                createNewKey(finalPlatformName, isValid = false, onSuccess = onSuccess)
                return@launch
            }
            
            // Если платформа поддерживает валидацию - валидируем
            if (supportsValidation()) {
                validateAndCreateKey(finalPlatformName, onSuccess)
            } else {
                // Для остальных платформ - просто сохраняем
                _uiState.update { it.copy(isSaving = true) }
                createNewKey(finalPlatformName, isValid = false, onSuccess = onSuccess)
            }
        }
    }
    
    /**
     * Валидировать и создать ключ
     */
    private suspend fun validateAndCreateKey(platformName: String, onSuccess: () -> Unit) {
        val state = _uiState.value
        
        _uiState.update { it.copy(isValidating = true) }
        
        val result = KeyValidationService.validateKey(platformName, state.apiKeyValue)
        
        _uiState.update { it.copy(isValidating = false) }
        
        when (result) {
            is ValidationResult.Valid -> {
                // Показываем "Ключ работает"
                _uiState.update { it.copy(validationSuccess = true) }
                
                // Через 1 секунду сохраняем и закрываем
                delay(1000)
                createNewKey(platformName, isValid = true, onSuccess = onSuccess)
            }
            
            is ValidationResult.Invalid,
            is ValidationResult.ServerError,
            is ValidationResult.NetworkError -> {
                // Валидация не прошла — меняем кнопку на "Сохранить ключ"
                _uiState.update { it.copy(validationFailed = true) }
            }
        }
    }
    
    /**
     * Создать новый ключ
     */
    private suspend fun createNewKey(platformName: String, isValid: Boolean, onSuccess: () -> Unit) {
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
                isValid = isValid
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
    private suspend fun updateExistingKey(platformName: String, onSuccess: () -> Unit) {
        val state = _uiState.value
        val editingKey = state.editingKey ?: return
        
        try {
            // Найти или создать платформу
            val platform = repository.getOrCreatePlatform(
                name = platformName,
                customIconData = state.customIconData
            )
            
            // Обновляем метаданные
            val noteValue = state.note.trim().ifBlank { null }
            var updatedKey = editingKey.copy(
                myName = state.myName,
                note = noteValue,
                platformId = platform.id
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
                
                updatedKey = updatedKey.copy(isValid = false)
            }
            
            repository.updateKey(updatedKey)
            
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
                showError = true,
                isValidating = false,
                isSaving = false
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
        val platformValid = if (state.selectedPlatformName == "New") {
            state.customPlatformName.isNotBlank()
        } else {
            state.selectedPlatformName.isNotBlank()
        }
        
        return state.myName.isNotBlank() && 
               state.apiKeyValue.isNotBlank() && 
               platformValid
    }
    
    /**
     * Проверить, заблокирована ли кнопка
     */
    fun isButtonDisabled(): Boolean {
        val state = _uiState.value
        return !isFormValid() || state.isValidating || state.validationSuccess
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
