package com.example.byokvault.data.model

import androidx.annotation.DrawableRes
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.byokvault.R
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
         * Маппинг имен платформ на ID drawable ресурсов
         */
        @DrawableRes
        fun getIconResId(platformName: String): Int? {
            return when (platformName) {
                "Anthropic" -> R.drawable.anthropic
                "OpenAI" -> R.drawable.openai
                "Gemini" -> R.drawable.gemini
                "Hailuo" -> R.drawable.hailuo
                "DeepSeek" -> R.drawable.deepseek
                "Reve AI" -> R.drawable.reveai
                "GitHub" -> R.drawable.github
                "Google Image Search" -> R.drawable.google_image_search
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
     * ID иконки в drawable для предустановленных платформ
     */
    @get:DrawableRes
    val iconResId: Int?
        get() = if (isDefault) getIconResId(name) else null
}
