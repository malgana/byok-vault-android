package com.example.byokvault.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Цветовая схема приложения - соответствует iOS версии
 * Зелёный акцент + градиентные фоны
 */

// Основной зелёный цвет (как в iOS)
val GreenPrimary = Color(0xFF34C759)
val GreenDark = Color(0xFF30B350)

// Фиолетовый для градиентов и акцентов
val PurpleAccent = Color(0xFF8B5CF6)
val BlueAccent = Color(0xFF3B82F6)

// Цвета для светлой темы
private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD4F4DD),
    onPrimaryContainer = Color(0xFF002106),
    
    secondary = Color(0xFF52634F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD5E8CE),
    onSecondaryContainer = Color(0xFF101F0F),
    
    tertiary = PurpleAccent,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE9DDFF),
    onTertiaryContainer = Color(0xFF22005D),
    
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onError = Color.White,
    onErrorContainer = Color(0xFF410002),
    
    // Прозрачный фон для градиента
    background = Color.Transparent,
    onBackground = Color(0xFF1A1C19),
    
    surface = Color(0xFFFCFDF6),
    onSurface = Color(0xFF1A1C19),
    surfaceVariant = Color(0xFFDEE5D8),
    onSurfaceVariant = Color(0xFF424940),
    
    outline = Color(0xFF72796F),
    outlineVariant = Color(0xFFC2C9BD),
    
    inverseSurface = Color(0xFF2F312D),
    inverseOnSurface = Color(0xFFF0F1EB),
    inversePrimary = GreenPrimary
)

// Цвета для тёмной темы
private val DarkColorScheme = darkColorScheme(
    primary = GreenPrimary,
    onPrimary = Color(0xFF00390F),
    primaryContainer = Color(0xFF005320),
    onPrimaryContainer = Color(0xFFD4F4DD),
    
    secondary = Color(0xFFB9CCB3),
    onSecondary = Color(0xFF243423),
    secondaryContainer = Color(0xFF3A4B38),
    onSecondaryContainer = Color(0xFFD5E8CE),
    
    tertiary = Color(0xFFCFBDFE),
    onTertiary = Color(0xFF381E72),
    tertiaryContainer = Color(0xFF4F378A),
    onTertiaryContainer = Color(0xFFE9DDFF),
    
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onError = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6),
    
    // Прозрачный фон для градиента
    background = Color.Transparent,
    onBackground = Color(0xFFE2E3DD),
    
    surface = Color(0xFF1A1C19),
    onSurface = Color(0xFFE2E3DD),
    surfaceVariant = Color(0xFF424940),
    onSurfaceVariant = Color(0xFFC2C9BD),
    
    outline = Color(0xFF8C9388),
    outlineVariant = Color(0xFF424940),
    
    inverseSurface = Color(0xFFE2E3DD),
    inverseOnSurface = Color(0xFF1A1C19),
    inversePrimary = Color(0xFF006E25)
)

@Composable
fun BYOKVaultTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Отключаем dynamic color для консистентности с iOS
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Прозрачный статус бар для градиентного фона
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
