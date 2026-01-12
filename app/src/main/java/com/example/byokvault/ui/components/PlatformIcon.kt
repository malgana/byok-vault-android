package com.example.byokvault.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.byokvault.data.model.Platform
import com.example.byokvault.utils.ImageHelper

/**
 * Компонент для отображения иконки платформы
 * Аналог PlatformIconView.swift из iOS версии
 * 
 * @param platform Платформа
 * @param size Размер иконки в dp
 * @param modifier Модификатор
 */
@Composable
fun PlatformIcon(
    platform: Platform,
    size: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        when {
            // Кастомная иконка пользователя
            platform.customIconData != null -> {
                val bitmap = ImageHelper.base64ToBitmap(platform.customIconData)
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = platform.name,
                        modifier = Modifier.size(size),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Fallback если не удалось декодировать
                    DefaultPlatformIcon(size = size)
                }
            }
            
            // Предустановленная иконка из drawable
            platform.isDefault && platform.assetIconName != null -> {
                val resourceId = context.resources.getIdentifier(
                    platform.assetIconName,
                    "drawable",
                    context.packageName
                )
                
                if (resourceId != 0) {
                    // TODO: Загрузка иконки из drawable (добавим на следующем этапе)
                    // Пока используем дефолтную иконку
                    DefaultPlatformIcon(size = size)
                } else {
                    DefaultPlatformIcon(size = size)
                }
            }
            
            // Дефолтная иконка
            else -> {
                DefaultPlatformIcon(size = size)
            }
        }
    }
}

/**
 * Дефолтная иконка для платформы
 */
@Composable
private fun DefaultPlatformIcon(size: Dp) {
    Icon(
        imageVector = Icons.Default.CloudQueue,
        contentDescription = null,
        modifier = Modifier.size(size * 0.6f),
        tint = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
