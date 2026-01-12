package com.example.byokvault.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Sealed класс для навигации между экранами
 * Использует type-safe navigation с Kotlin Serialization
 */
sealed class Screen {
    
    /**
     * Главный экран со списком платформ
     */
    @Serializable
    data object Main : Screen()
    
    /**
     * Экран добавления/редактирования ключа
     * 
     * @param keyId ID ключа для редактирования (null для создания нового)
     * @param platformId ID предвыбранной платформы (опционально)
     */
    @Serializable
    data class AddKey(
        val keyId: Long? = null,
        val platformId: Long? = null
    ) : Screen()
    
    /**
     * Экран деталей ключа
     * 
     * @param keyId ID ключа
     */
    @Serializable
    data class KeyDetail(val keyId: Long) : Screen()
    
    /**
     * Экран списка ключей платформы
     * 
     * @param platformId ID платформы
     */
    @Serializable
    data class PlatformKeysList(val platformId: Long) : Screen()
}
