package com.example.byokvault.data.validation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Сервис валидации API ключей Google Gemini
 * Аналог GeminiService.swift из iOS версии
 */
object GeminiService {
    
    private const val API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent"
    
    /**
     * Валидация API ключа через минимальный запрос
     */
    suspend fun validateAPIKey(apiKey: String): ValidationResult = withContext(Dispatchers.IO) {
        try {
            val url = URL("$API_URL?key=$apiKey")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                connectTimeout = 15000
                readTimeout = 15000
            }
            
            val body = JSONObject().apply {
                put("contents", org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", org.json.JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "Hi")
                            })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("maxOutputTokens", 1)
                })
            }
            
            connection.outputStream.use { os ->
                os.write(body.toString().toByteArray())
            }
            
            val responseCode = connection.responseCode
            
            when (responseCode) {
                200 -> ValidationResult.Valid
                400 -> {
                    try {
                        val errorStream = connection.errorStream
                        val errorBody = errorStream?.bufferedReader()?.readText()
                        errorStream?.close()
                        
                        if (errorBody != null) {
                            val json = JSONObject(errorBody)
                            val error = json.optJSONObject("error")
                            val message = error?.optString("message") ?: ""
                            if (message.contains("API key", ignoreCase = true)) {
                                return@withContext ValidationResult.Invalid("Неверный API ключ")
                            }
                            if (message.isNotBlank()) {
                                return@withContext ValidationResult.Invalid(message)
                            }
                        }
                    } catch (_: Exception) {}
                    ValidationResult.Invalid("Неверный запрос")
                }
                401, 403 -> ValidationResult.Invalid("Неверный API ключ")
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
