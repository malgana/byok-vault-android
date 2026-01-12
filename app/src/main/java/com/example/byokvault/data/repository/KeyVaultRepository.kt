package com.example.byokvault.data.repository

import com.example.byokvault.data.dao.APIKeyDao
import com.example.byokvault.data.dao.PlatformDao
import com.example.byokvault.data.model.APIKey
import com.example.byokvault.data.model.Platform
import com.example.byokvault.data.model.PlatformWithKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Репозиторий для работы с данными приложения
 * Предоставляет единый интерфейс для доступа к базе данных
 */
class KeyVaultRepository(
    private val platformDao: PlatformDao,
    private val apiKeyDao: APIKeyDao
) {
    
    // ========== Platform operations ==========
    
    /**
     * Получить все платформы с ключами
     */
    fun getAllPlatformsWithKeys(): Flow<List<PlatformWithKeys>> {
        return platformDao.getAllPlatformsWithKeys()
    }
    
    /**
     * Получить только платформы, у которых есть ключи
     */
    fun getPlatformsWithKeysOnly(): Flow<List<PlatformWithKeys>> {
        return platformDao.getAllPlatformsWithKeys()
            .map { list -> list.filter { it.apiKeys.isNotEmpty() } }
    }
    
    /**
     * Получить платформу по ID с её ключами
     */
    fun getPlatformWithKeys(platformId: Long): Flow<PlatformWithKeys?> {
        return platformDao.getPlatformWithKeys(platformId)
    }
    
    /**
     * Получить платформу по имени или создать новую
     */
    suspend fun getOrCreatePlatform(name: String, customIconData: String? = null): Platform {
        val existing = platformDao.getPlatformByName(name)
        if (existing != null) {
            return existing
        }
        
        val newPlatform = Platform(
            name = name,
            customIconData = customIconData
        )
        val id = platformDao.insertPlatform(newPlatform)
        return newPlatform.copy(id = id)
    }
    
    /**
     * Вставить платформу
     */
    suspend fun insertPlatform(platform: Platform): Long {
        return platformDao.insertPlatform(platform)
    }
    
    /**
     * Обновить платформу
     */
    suspend fun updatePlatform(platform: Platform) {
        platformDao.updatePlatform(platform)
    }
    
    /**
     * Удалить платформу
     */
    suspend fun deletePlatform(platform: Platform) {
        platformDao.deletePlatform(platform)
    }
    
    /**
     * Удалить пустые пользовательские платформы
     */
    suspend fun cleanupEmptyCustomPlatforms() {
        platformDao.deleteEmptyCustomPlatforms(Platform.defaultPlatforms)
    }
    
    /**
     * Получить количество ключей для платформы
     */
    suspend fun getKeyCountForPlatform(platformId: Long): Int {
        return platformDao.getKeyCountForPlatform(platformId)
    }
    
    // ========== APIKey operations ==========
    
    /**
     * Получить все ключи
     */
    fun getAllKeys(): Flow<List<APIKey>> {
        return apiKeyDao.getAllKeys()
    }
    
    /**
     * Получить ключ по ID
     */
    suspend fun getKeyById(keyId: Long): APIKey? {
        return apiKeyDao.getKeyById(keyId)
    }
    
    /**
     * Получить ключ по keystoreId
     */
    suspend fun getKeyByKeystoreId(keystoreId: String): APIKey? {
        return apiKeyDao.getKeyByKeystoreId(keystoreId)
    }
    
    /**
     * Получить ключи для платформы
     */
    fun getKeysForPlatform(platformId: Long): Flow<List<APIKey>> {
        return apiKeyDao.getKeysForPlatform(platformId)
    }
    
    /**
     * Вставить новый ключ
     */
    suspend fun insertKey(apiKey: APIKey): Long {
        return apiKeyDao.insertKey(apiKey)
    }
    
    /**
     * Обновить ключ
     */
    suspend fun updateKey(apiKey: APIKey) {
        apiKeyDao.updateKey(apiKey)
    }
    
    /**
     * Удалить ключ
     */
    suspend fun deleteKey(apiKey: APIKey) {
        apiKeyDao.deleteKey(apiKey)
    }
    
    /**
     * Проверить существование ключа
     */
    suspend fun keyExists(keystoreId: String): Boolean {
        return apiKeyDao.keyExists(keystoreId)
    }
}
