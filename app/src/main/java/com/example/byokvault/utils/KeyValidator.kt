package com.example.byokvault.utils

import com.example.byokvault.data.keystore.KeystoreService
import com.example.byokvault.data.model.APIKey
import com.example.byokvault.data.repository.KeyVaultRepository
import kotlinx.coroutines.flow.firstOrNull

/**
 * Утилита для валидации API ключей
 */
class KeyValidator(
    private val repository: KeyVaultRepository,
    private val keystoreService: KeystoreService
) {
    
    /**
     * Результат проверки на дубликат
     */
    sealed class DuplicateCheckResult {
        data object NotDuplicate : DuplicateCheckResult()
        data class Duplicate(val existingKey: APIKey, val platformName: String) : DuplicateCheckResult()
    }
    
    /**
     * Проверить, не является ли ключ дубликатом
     * 
     * @param keyValue Значение API ключа для проверки
     * @param excludeKeystoreId Исключить из проверки (для редактирования)
     * @return результат проверки
     */
    suspend fun checkForDuplicate(
        keyValue: String,
        excludeKeystoreId: String? = null
    ): DuplicateCheckResult {
        // Получаем все keystoreId из базы
        val allStoredIdentifiers = keystoreService.getAllStoredIdentifiers()
        
        // Проверяем каждый сохраненный ключ
        for (identifier in allStoredIdentifiers) {
            // Пропускаем, если это тот же ключ (при редактировании)
            if (identifier == excludeKeystoreId) continue
            
            // Получаем значение из keystore
            val storedValue = keystoreService.get(identifier) ?: continue
            
            // Сравниваем значения
            if (storedValue == keyValue) {
                // Найден дубликат - получаем информацию о ключе
                val existingKey = repository.getKeyByKeystoreId(identifier)
                if (existingKey != null) {
                    val platformWithKeys = repository.getPlatformWithKeys(existingKey.platformId).firstOrNull()
                    val platformName = platformWithKeys?.platform?.name ?: "Неизвестно"
                    return DuplicateCheckResult.Duplicate(existingKey, platformName)
                }
            }
        }
        
        return DuplicateCheckResult.NotDuplicate
    }
    
    /**
     * Проверить формат API ключа (базовая проверка)
     * 
     * @param keyValue Значение ключа
     * @return true если формат корректный
     */
    fun isValidFormat(keyValue: String): Boolean {
        return keyValue.isNotBlank() && keyValue.length >= 10
    }
}
