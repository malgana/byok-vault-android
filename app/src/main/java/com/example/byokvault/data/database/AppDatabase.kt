package com.example.byokvault.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.byokvault.data.dao.APIKeyDao
import com.example.byokvault.data.dao.PlatformDao
import com.example.byokvault.data.model.APIKey
import com.example.byokvault.data.model.Platform

/**
 * Главная база данных приложения
 * Аналог ModelContainer из SwiftData
 */
@Database(
    entities = [Platform::class, APIKey::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun platformDao(): PlatformDao
    abstract fun apiKeyDao(): APIKeyDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "byok_vault_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
