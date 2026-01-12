package com.example.byokvault.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * API ключ для определенной платформы
 * Аналог APIKey.swift из iOS версии
 * 
 * Сам ключ хранится в Android Keystore (зашифрованно),
 * здесь только метаданные
 */
@Entity(
    tableName = "api_keys",
    foreignKeys = [
        ForeignKey(
            entity = Platform::class,
            parentColumns = ["id"],
            childColumns = ["platformId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("platformId")]
)
data class APIKey(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Пользовательское название ключа
    val myName: String,
    
    // UUID для доступа к ключу в Keystore
    val keystoreId: String,
    
    // ID платформы
    val platformId: Long,
    
    val dateAdded: Long = System.currentTimeMillis(),
    
    // Прошел ли ключ валидацию
    var isValid: Boolean = false,
    
    // Заметка к ключу (опционально)
    val note: String? = null
)
