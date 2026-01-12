package com.example.byokvault

import android.app.Application
import com.example.byokvault.data.database.AppDatabase
import com.example.byokvault.data.keystore.KeystoreService
import com.example.byokvault.data.repository.KeyVaultRepository

/**
 * Главный Application класс
 * Инициализирует базу данных, репозиторий и keystore
 */
class KeyVaultApplication : Application() {
    
    // База данных
    val database: AppDatabase by lazy { 
        AppDatabase.getDatabase(this) 
    }
    
    // Keystore для безопасного хранения API ключей
    val keystoreService: KeystoreService by lazy {
        KeystoreService.getInstance(this)
    }
    
    // Репозиторий
    val repository: KeyVaultRepository by lazy {
        KeyVaultRepository(
            platformDao = database.platformDao(),
            apiKeyDao = database.apiKeyDao()
        )
    }
}
