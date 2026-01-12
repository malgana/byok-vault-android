package com.example.byokvault.data.dao

import androidx.room.*
import com.example.byokvault.data.model.APIKey
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object для работы с API ключами
 */
@Dao
interface APIKeyDao {
    /**
     * Получить все ключи
     */
    @Query("SELECT * FROM api_keys ORDER BY dateAdded DESC")
    fun getAllKeys(): Flow<List<APIKey>>
    
    /**
     * Получить ключ по ID
     */
    @Query("SELECT * FROM api_keys WHERE id = :keyId")
    suspend fun getKeyById(keyId: Long): APIKey?
    
    /**
     * Получить ключ по keystoreId
     */
    @Query("SELECT * FROM api_keys WHERE keystoreId = :keystoreId LIMIT 1")
    suspend fun getKeyByKeystoreId(keystoreId: String): APIKey?
    
    /**
     * Получить все ключи для конкретной платформы
     */
    @Query("SELECT * FROM api_keys WHERE platformId = :platformId ORDER BY dateAdded DESC")
    fun getKeysForPlatform(platformId: Long): Flow<List<APIKey>>
    
    /**
     * Вставить новый ключ
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKey(apiKey: APIKey): Long
    
    /**
     * Обновить ключ
     */
    @Update
    suspend fun updateKey(apiKey: APIKey)
    
    /**
     * Удалить ключ
     */
    @Delete
    suspend fun deleteKey(apiKey: APIKey)
    
    /**
     * Удалить все ключи платформы
     */
    @Query("DELETE FROM api_keys WHERE platformId = :platformId")
    suspend fun deleteAllKeysForPlatform(platformId: Long)
    
    /**
     * Проверить существование ключа с таким keystoreId
     */
    @Query("SELECT EXISTS(SELECT 1 FROM api_keys WHERE keystoreId = :keystoreId)")
    suspend fun keyExists(keystoreId: String): Boolean
}
