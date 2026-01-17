package com.example.byokvault.data.validation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Сервис валидации API ключей Hailuo (MiniMax)
 * Аналог HailuoService.swift из iOS версии
 */
object HailuoService {
    
    private const val API_URL = "https://api.minimax.io/v1/files/retrieve?GroupId=1956997081382003480&file_id=test_invalid_id"
    
    /**
     * Валидация API ключа через запрос к files endpoint
     */
    suspend fun validateAPIKey(apiKey: String): ValidationResult = withContext(Dispatchers.IO) {
        try {
            val url = URL(API_URL)
            val connection = url.openConnection() as HttpURLConnection
            
            connection.apply {
                requestMethod = "GET"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer $apiKey")
                connectTimeout = 15000
                readTimeout = 15000
            }
            
            val responseCode = connection.responseCode
            
            // Читаем ответ
            val responseBody = try {
                if (responseCode in 200..299) {
                    connection.inputStream.bufferedReader().readText()
                } else {
                    connection.errorStream?.bufferedReader()?.readText()
                }
            } catch (_: Exception) { null }
            
            // Проверяем base_resp на ошибки авторизации
            if (responseBody != null) {
                try {
                    val json = JSONObject(responseBody)
                    val baseResp = json.optJSONObject("base_resp")
                    if (baseResp != null) {
                        val statusCode = baseResp.optInt("status_code", -1)
                        val statusMsg = baseResp.optString("status_msg", "")
                        
                        // Проверяем сообщение на ошибки авторизации
                        val isAuthError = statusMsg.lowercase().let { msg ->
                            msg.contains("login fail") ||
                            msg.contains("invalid api") ||
                            msg.contains("authorization") ||
                            msg.contains("api key") ||
                            msg.contains("api secret")
                        }
                        
                        if (isAuthError) {
                            return@withContext ValidationResult.Invalid("Неверный API ключ")
                        }
                        
                        // Коды ошибок авторизации MiniMax
                        if (statusCode == 1001 || statusCode == 1002 || statusCode == 2049) {
                            return@withContext ValidationResult.Invalid(
                                if (statusMsg.isBlank()) "Неверный API ключ" else statusMsg
                            )
                        }
                        
                        // Успешные коды
                        if (statusCode == 0 || statusCode == 2013) {
                            return@withContext ValidationResult.Valid
                        }
                        
                        // 1004 может быть и "file not found" и "login fail"
                        if (statusCode == 1004 && !isAuthError) {
                            return@withContext ValidationResult.Valid
                        }
                    }
                } catch (_: Exception) {}
            }
            
            when (responseCode) {
                in 200..299 -> ValidationResult.Valid
                400 -> ValidationResult.Invalid("Неверный запрос")
                401 -> ValidationResult.Invalid("Неверный API ключ")
                403 -> ValidationResult.Invalid("Ключ заблокирован")
                429 -> ValidationResult.Valid
                500, 502, 503 -> ValidationResult.ServerError("Сервер недоступен")
                else -> ValidationResult.ServerError("Код ошибки: $responseCode")
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
