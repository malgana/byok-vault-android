package com.example.byokvault.data.keystore

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Сервис для безопасного хранения API ключей
 * Аналог iOS Keychain - использует Android Keystore System
 * 
 * Использует EncryptedSharedPreferences для автоматического
 * шифрования/дешифрования данных с помощью Android Keystore
 */
class KeystoreService(context: Context) {
    
    private val masterKey: MasterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_FILENAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    /**
     * Сохранить API ключ
     * 
     * @param key API ключ (значение)
     * @param identifier Уникальный идентификатор (keystoreId)
     * @return true если успешно сохранено
     */
    fun save(key: String, identifier: String): Boolean {
        return try {
            encryptedPreferences.edit()
                .putString(identifier, key)
                .commit()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Получить API ключ
     * 
     * @param identifier Уникальный идентификатор (keystoreId)
     * @return API ключ или null если не найден
     */
    fun get(identifier: String): String? {
        return try {
            encryptedPreferences.getString(identifier, null)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Удалить API ключ
     * 
     * @param identifier Уникальный идентификатор (keystoreId)
     * @return true если успешно удалено
     */
    fun delete(identifier: String): Boolean {
        return try {
            encryptedPreferences.edit()
                .remove(identifier)
                .commit()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Обновить API ключ
     * 
     * @param key Новое значение ключа
     * @param identifier Уникальный идентификатор (keystoreId)
     * @return true если успешно обновлено
     */
    fun update(key: String, identifier: String): Boolean {
        // В SharedPreferences update = save
        return save(key, identifier)
    }
    
    /**
     * Получить все сохраненные идентификаторы (keystoreId)
     * 
     * @return Список всех keystoreId
     */
    fun getAllStoredIdentifiers(): List<String> {
        return try {
            encryptedPreferences.all.keys.toList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * Проверить существование ключа
     * 
     * @param identifier Уникальный идентификатор (keystoreId)
     * @return true если ключ существует
     */
    fun exists(identifier: String): Boolean {
        return encryptedPreferences.contains(identifier)
    }
    
    /**
     * Очистить все ключи (использовать осторожно!)
     */
    fun clearAll(): Boolean {
        return try {
            encryptedPreferences.edit()
                .clear()
                .commit()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    companion object {
        private const val PREFS_FILENAME = "byok_vault_encrypted_prefs"
        
        @Volatile
        private var INSTANCE: KeystoreService? = null
        
        fun getInstance(context: Context): KeystoreService {
            return INSTANCE ?: synchronized(this) {
                val instance = KeystoreService(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
