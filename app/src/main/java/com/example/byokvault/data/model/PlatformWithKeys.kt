package com.example.byokvault.data.model

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Связь Platform с её API ключами (один-ко-многим)
 * Используется для запросов с JOIN
 */
data class PlatformWithKeys(
    @Embedded val platform: Platform,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "platformId"
    )
    val apiKeys: List<APIKey> = emptyList()
)
