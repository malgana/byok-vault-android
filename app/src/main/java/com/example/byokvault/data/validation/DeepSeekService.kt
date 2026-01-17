package com.example.byokvault.data.validation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Сервис валидации API ключей DeepSeek
 * Аналог DeepSeekService.swift из iOS версии
 */
object DeepSeekService {
    
    private const val API_URL = "https://api.deepseek.com/v1/chat/completions"
    
    /**
     * Валидация API ключа через минимальный запрос
     */
    suspend fun validateAPIKey(apiKey: String): ValidationResult = withContext(Dispatchers.IO) {
        try {
            val url = URL(API_URL)
            val connection = url.openConnection() as HttpURLConnection
            
            connection.apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer $apiKey")
                doOutput = true
                connectTimeout = 15000
                readTimeout = 15000
            }
            
            val body = JSONObject().apply {
                put("model", "deepseek-chat")
                put("max_tokens", 1)
                put("messages", org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", "Hi")
                    })
                })
                put("stream", false)
            }
            
            connection.outputStream.use { os ->
                os.write(body.toString().toByteArray())
            }
            
            val responseCode = connection.responseCode
            
            when (responseCode) {
                200 -> ValidationResult.Valid
                401 -> ValidationResult.Invalid("Неверный API ключ")
                403 -> ValidationResult.Invalid("Ключ заблокирован")
                429 -> ValidationResult.Valid
                500, 502, 503 -> ValidationResult.ServerError("Сервер недоступен")
                else -> {
                    try {
                        val errorStream = connection.errorStream
                        val errorBody = errorStream?.bufferedReader()?.readText()
                        errorStream?.close()
                        
                        if (errorBody != null) {
                            val json = JSONObject(errorBody)
                            val error = json.optJSONObject("error")
                            val message = error?.optString("message")
                            if (!message.isNullOrBlank()) {
                                return@withContext ValidationResult.Invalid(message)
                            }
                        }
                    } catch (_: Exception) {}
                    
                    ValidationResult.ServerError("Код ошибки: $responseCode")
                }
            }
        } catch (e: java.net.UnknownHostException) {
            ValidationResult.NetworkError("Нет подключения к сети")
        } catch (e: java.net.SocketTimeoutException) {
            ValidationResult.NetworkError("Превышено время ожидания")
        } catch (e: Exception) {
            ValidationResult.NetworkError("Ошибка сети: ${e.message}")
        }
    }
}
