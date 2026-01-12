package com.example.byokvault.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Платформа/сервис для хранения API ключей
 * Аналог Platform.swift из iOS версии
 */
@Entity(tableName = "platforms")
data class Platform(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val name: String,
    
    val dateCreated: Long = System.currentTimeMillis(),
    
    // Кастомная иконка пользователя (сжатое изображение в Base64 или путь)
    val customIconData: String? = null
) {
    companion object {
        /**
         * Предустановленные платформы с встроенными иконками
         */
        val defaultPlatforms = listOf(
            "Anthropic",
            "OpenAI",
            "Gemini",
            "Hailuo",
            "DeepSeek",
            "Reve AI",
            "GitHub",
            "Google Image Search"
        )
        
        /**
         * Маппинг имен платформ на имена drawable ресурсов
         */
        fun getAssetIconName(platformName: String): String? {
            return when (platformName) {
                "Anthropic" -> "anthropic"
                "OpenAI" -> "openai"
                "Gemini" -> "google_ai"
                "Hailuo" -> "hailuo"
                "DeepSeek" -> "deepseek"
                "Reve AI" -> "reve_ai"
                "GitHub" -> "github"
                "Google Image Search" -> "google_image_search"
                else -> null
            }
        }
    }
    
    /**
     * Проверка, является ли платформа предустановленной
     */
    val isDefault: Boolean
        get() = defaultPlatforms.contains(name)
    
    /**
     * Имя иконки в drawable для предустановленных платформ
     */
    val assetIconName: String?
        get() = if (isDefault) getAssetIconName(name) else null
}
