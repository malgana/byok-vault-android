package com.example.byokvault.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.byokvault.data.model.Platform
import com.example.byokvault.utils.ImageHelper

/**
 * Иконка платформы с поддержкой кастомных иконок
 */
@Composable
fun PlatformIcon(
    platform: Platform?,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(8.dp)

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        // Проверяем, есть ли кастомная иконка
        if (platform?.customIconData != null) {
            val bitmap = ImageHelper.base64ToBitmap(platform.customIconData)
            if (bitmap != null) {
                Image(
                    painter = BitmapPainter(bitmap.asImageBitmap()),
                    contentDescription = "${platform.name} icon",
                    modifier = Modifier.size(size),
                    contentScale = ContentScale.Crop
                )
                return@Box
            }
        }
        
        // Проверяем, есть ли предустановленная иконка
        val iconRes = platform?.iconResId
        if (iconRes != null) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = "${platform.name} icon",
                modifier = Modifier.size(size)
            )
            return@Box
        }

        // Иконка по умолчанию
        Icon(
            imageVector = Icons.Default.Language,
            contentDescription = "Default platform icon",
            modifier = Modifier.size(size * 0.6f),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
