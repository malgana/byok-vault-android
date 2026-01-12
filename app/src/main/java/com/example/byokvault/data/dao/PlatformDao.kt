package com.example.byokvault.data.dao

import androidx.room.*
import com.example.byokvault.data.model.Platform
import com.example.byokvault.data.model.PlatformWithKeys
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object для работы с платформами
 */
@Dao
interface PlatformDao {
    /**
     * Получить все платформы с их ключами, отсортированные по имени
     */
    @Transaction
    @Query("SELECT * FROM platforms ORDER BY name ASC")
    fun getAllPlatformsWithKeys(): Flow<List<PlatformWithKeys>>
    
    /**
     * Получить платформу по ID с её ключами
     */
    @Transaction
    @Query("SELECT * FROM platforms WHERE id = :platformId")
    fun getPlatformWithKeys(platformId: Long): Flow<PlatformWithKeys?>
    
    /**
     * Получить платформу по имени
     */
    @Query("SELECT * FROM platforms WHERE name = :name LIMIT 1")
    suspend fun getPlatformByName(name: String): Platform?
    
    /**
     * Вставить новую платформу
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlatform(platform: Platform): Long
    
    /**
     * Обновить платформу
     */
    @Update
    suspend fun updatePlatform(platform: Platform)
    
    /**
     * Удалить платформу (каскадно удалит все связанные ключи)
     */
    @Delete
    suspend fun deletePlatform(platform: Platform)
    
    /**
     * Удалить пустые пользовательские платформы
     */
    @Query("""
        DELETE FROM platforms 
        WHERE id NOT IN (SELECT DISTINCT platformId FROM api_keys)
        AND name NOT IN (:defaultPlatforms)
    """)
    suspend fun deleteEmptyCustomPlatforms(defaultPlatforms: List<String>)
    
    /**
     * Получить количество ключей для платформы
     */
    @Query("SELECT COUNT(*) FROM api_keys WHERE platformId = :platformId")
    suspend fun getKeyCountForPlatform(platformId: Long): Int
}
