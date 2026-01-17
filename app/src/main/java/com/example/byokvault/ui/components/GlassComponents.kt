package com.example.byokvault.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Градиентный фон как в iOS версии
 */
@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    
    val gradientColors = if (isDark) {
        listOf(
            Color(0xFF0D0D26), // Тёмно-синий
            Color(0xFF1A1433), // Фиолетовый оттенок
            Color.Black
        )
    } else {
        listOf(
            Color(0xFFF2F2FF), // Светло-лавандовый
            Color(0xFFE6EBFF), // Светло-голубой
            Color(0xFFD9E0F2)  // Светло-серо-голубой
        )
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = gradientColors,
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(
                        Float.POSITIVE_INFINITY,
                        Float.POSITIVE_INFINITY
                    )
                )
            ),
        content = content
    )
}

/**
 * Стеклянная карточка (GlassCard) как в iOS версии
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    cornerRadius: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = 300f
        ),
        label = "scale"
    )
    
    // Цвета для стеклянного эффекта
    val glassColor = if (isDark) {
        Color.White.copy(alpha = 0.08f)
    } else {
        Color.White.copy(alpha = 0.6f)
    }
    
    val borderColors = if (isDark) {
        listOf(
            Color.White.copy(alpha = 0.3f),
            Color.White.copy(alpha = 0.1f)
        )
    } else {
        listOf(
            Color.White.copy(alpha = 0.8f),
            Color.White.copy(alpha = 0.3f)
        )
    }
    
    val shadowColor = if (isDark) {
        Color.Black.copy(alpha = 0.4f)
    } else {
        Color.Black.copy(alpha = 0.1f)
    }
    
    val shape = RoundedCornerShape(cornerRadius)
    
    Column(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isDark) 16.dp else 12.dp,
                shape = shape,
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(shape)
            .background(glassColor)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = borderColors,
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(
                        Float.POSITIVE_INFINITY,
                        Float.POSITIVE_INFINITY
                    )
                ),
                shape = shape
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            ),
        content = content
    )
}

/**
 * Стеклянная карточка для списка ключей
 */
@Composable
fun GlassListCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    cornerRadius: Dp = 16.dp,
    content: @Composable RowScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = 300f
        ),
        label = "scale"
    )
    
    val glassColor = if (isDark) {
        Color.White.copy(alpha = 0.08f)
    } else {
        Color.White.copy(alpha = 0.6f)
    }
    
    val borderColors = if (isDark) {
        listOf(
            Color.White.copy(alpha = 0.3f),
            Color.White.copy(alpha = 0.1f)
        )
    } else {
        listOf(
            Color.White.copy(alpha = 0.8f),
            Color.White.copy(alpha = 0.3f)
        )
    }
    
    val shadowColor = if (isDark) {
        Color.Black.copy(alpha = 0.4f)
    } else {
        Color.Black.copy(alpha = 0.1f)
    }
    
    val shape = RoundedCornerShape(cornerRadius)
    
    Row(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isDark) 12.dp else 8.dp,
                shape = shape,
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(shape)
            .background(glassColor)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = borderColors,
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(
                        Float.POSITIVE_INFINITY,
                        Float.POSITIVE_INFINITY
                    )
                ),
                shape = shape
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            ),
        content = content
    )
}

/**
 * Стеклянная кнопка в тулбаре
 */
@Composable
fun GlassIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = 400f
        ),
        label = "scale"
    )
    
    val glassColor = if (isDark) {
        Color.White.copy(alpha = 0.1f)
    } else {
        Color.White.copy(alpha = 0.7f)
    }
    
    Box(
        modifier = modifier
            .scale(scale)
            .size(size)
            .clip(RoundedCornerShape(size / 2))
            .background(glassColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = androidx.compose.ui.Alignment.Center,
        content = content
    )
}
