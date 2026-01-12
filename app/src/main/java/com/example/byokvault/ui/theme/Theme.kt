package com.example.byokvault.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Цветовая схема приложения - зеленый акцент (как в iOS версии)
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF34C759), // Зеленый (как в iOS)
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD4F4DD),
    onPrimaryContainer = Color(0xFF002106),
    
    secondary = Color(0xFF52634F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD5E8CE),
    onSecondaryContainer = Color(0xFF101F0F),
    
    tertiary = Color(0xFF38656A),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFBCEBF0),
    onTertiaryContainer = Color(0xFF001F23),
    
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onError = Color.White,
    onErrorContainer = Color(0xFF410002),
    
    background = Color(0xFFFCFDF6),
    onBackground = Color(0xFF1A1C19),
    
    surface = Color(0xFFFCFDF6),
    onSurface = Color(0xFF1A1C19),
    surfaceVariant = Color(0xFFDEE5D8),
    onSurfaceVariant = Color(0xFF424940),
    
    outline = Color(0xFF72796F),
    inverseOnSurface = Color(0xFFF0F1EB),
    inverseSurface = Color(0xFF2F312D),
    inversePrimary = Color(0xFF34C759)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF34C759), // Зеленый
    onPrimary = Color(0xFF00390F),
    primaryContainer = Color(0xFF005320),
    onPrimaryContainer = Color(0xFFD4F4DD),
    
    secondary = Color(0xFFB9CCB3),
    onSecondary = Color(0xFF243423),
    secondaryContainer = Color(0xFF3A4B38),
    onSecondaryContainer = Color(0xFFD5E8CE),
    
    tertiary = Color(0xFFA0CFD4),
    onTertiary = Color(0xFF00363B),
    tertiaryContainer = Color(0xFF1E4D52),
    onTertiaryContainer = Color(0xFFBCEBF0),
    
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onError = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6),
    
    background = Color(0xFF1A1C19),
    onBackground = Color(0xFFE2E3DD),
    
    surface = Color(0xFF1A1C19),
    onSurface = Color(0xFFE2E3DD),
    surfaceVariant = Color(0xFF424940),
    onSurfaceVariant = Color(0xFFC2C9BD),
    
    outline = Color(0xFF8C9388),
    inverseOnSurface = Color(0xFF1A1C19),
    inverseSurface = Color(0xFFE2E3DD),
    inversePrimary = Color(0xFF006E25)
)

@Composable
fun BYOKVaultTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color доступен на Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
