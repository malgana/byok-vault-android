package com.example.byokvault.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.math.min

/**
 * Утилита для работы с изображениями
 * Аналог ImageHelper.swift из iOS версии
 */
object ImageHelper {
    
    private const val TARGET_SIZE = 250
    private const val COMPRESSION_QUALITY = 90
    
    /**
     * Обработать изображение: сжать до нужного размера и конвертировать в Base64
     * 
     * @param context Контекст
     * @param uri URI изображения
     * @return Base64 строка сжатого изображения или null
     */
    fun processImage(context: Context, uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            if (bitmap != null) {
                val resizedBitmap = resizeBitmap(bitmap, TARGET_SIZE)
                bitmapToBase64(resizedBitmap)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Изменить размер Bitmap с сохранением пропорций
     * 
     * @param bitmap Исходный bitmap
     * @param maxSize Максимальный размер (ширина или высота)
     * @return Измененный bitmap
     */
    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        if (width <= maxSize && height <= maxSize) {
            return bitmap
        }
        
        val scale = min(maxSize.toFloat() / width, maxSize.toFloat() / height)
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
    
    /**
     * Конвертировать Bitmap в Base64 строку
     * 
     * @param bitmap Bitmap для конвертации
     * @return Base64 строка
     */
    fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
    
    /**
     * Конвертировать Base64 строку в Bitmap
     * 
     * @param base64String Base64 строка
     * @return Bitmap или null
     */
    fun base64ToBitmap(base64String: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.NO_WRAP)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Получить размер изображения в килобайтах
     * 
     * @param base64String Base64 строка изображения
     * @return Размер в KB
     */
    fun getImageSizeInKB(base64String: String): Double {
        val decodedBytes = Base64.decode(base64String, Base64.NO_WRAP)
        return decodedBytes.size / 1024.0
    }
}
