package com.example.byokvault.data.validation

/**
 * Результат валидации API ключа
 * Аналог ValidationResult из iOS версии
 */
sealed class ValidationResult {
    /** Ключ валидный */
    data object Valid : ValidationResult()
    
    /** Ключ точно неверный - не сохраняем */
    data class Invalid(val message: String) : ValidationResult()
    
    /** Проблемы сервера - сохраняем без валидации */
    data class ServerError(val message: String) : ValidationResult()
    
    /** Нет сети - показываем ошибку */
    data class NetworkError(val message: String) : ValidationResult()
}
