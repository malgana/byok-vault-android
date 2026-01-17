package com.example.byokvault.data.validation

/**
 * Главный сервис валидации API ключей
 * Определяет какой сервис использовать для конкретной платформы
 */
object KeyValidationService {
    
    /**
     * Платформы, поддерживающие валидацию
     */
    val supportedPlatforms = listOf(
        "Anthropic",
        "DeepSeek", 
        "Gemini",
        "OpenAI",
        "Hailuo"
    )
    
    /**
     * Проверить, поддерживает ли платформа валидацию
     */
    fun supportsValidation(platformName: String): Boolean {
        return supportedPlatforms.contains(platformName)
    }
    
    /**
     * Валидировать API ключ для указанной платформы
     */
    suspend fun validateKey(platformName: String, apiKey: String): ValidationResult {
        return when (platformName) {
            "Anthropic" -> AnthropicService.validateAPIKey(apiKey)
            "DeepSeek" -> DeepSeekService.validateAPIKey(apiKey)
            "Gemini" -> GeminiService.validateAPIKey(apiKey)
            "OpenAI" -> OpenAIService.validateAPIKey(apiKey)
            "Hailuo" -> HailuoService.validateAPIKey(apiKey)
            else -> ValidationResult.ServerError("Платформа не поддерживает валидацию")
        }
    }
}
