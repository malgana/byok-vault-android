package com.example.byokvault.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.byokvault.data.model.Platform
import com.example.byokvault.utils.ImageHelper
import kotlin.math.abs

/**
 * Иконка платформы с поддержкой кастомных иконок
 * Обновлено для соответствия iOS версии с fallback на букву
 */
@Composable
fun PlatformIcon(
    platform: Platform?,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val cornerRadius = size * 0.2f
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 8.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.2f),
                spotColor = Color.Black.copy(alpha = 0.2f)
            )
            .clip(shape),
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
                modifier = Modifier.size(size),
                contentScale = ContentScale.Crop
            )
            return@Box
        }

        // Fallback - первая буква в цветном круге (как в iOS)
        FallbackIcon(
            name = platform?.name ?: "?",
            size = size
        )
    }
}

/**
 * Fallback иконка с первой буквой названия
 */
@Composable
private fun FallbackIcon(
    name: String,
    size: Dp
) {
    val firstLetter = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    val backgroundColor = remember(name) { colorForString(name) }
    
    Box(
        modifier = Modifier
            .size(size)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = firstLetter,
            color = Color.White,
            fontSize = (size.value * 0.5f).sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Генерирует цвет на основе строки (детерминированно)
 * Аналог colorForString из iOS версии
 */
private fun colorForString(string: String): Color {
    val colors = listOf(
        Color(0xFF007AFF), // systemBlue
        Color(0xFF34C759), // systemGreen
        Color(0xFF5856D6), // systemIndigo
        Color(0xFFFF9500), // systemOrange
        Color(0xFFFF2D55), // systemPink
        Color(0xFFAF52DE), // systemPurple
        Color(0xFFFF3B30), // systemRed
        Color(0xFF5AC8FA), // systemTeal
        Color(0xFFFFCC00), // systemYellow
        Color(0xFF32ADE6), // systemCyan
        Color(0xFF00C7BE), // systemMint
        Color(0xFFA2845E)  // systemBrown
    )
    
    val hash = abs(string.hashCode())
    val index = hash % colors.size
    return colors[index]
}
