package com.example.byokvault.ui.screens.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.byokvault.KeyVaultApplication
import com.example.byokvault.data.model.PlatformWithKeys
import com.example.byokvault.ui.components.GlassCard
import com.example.byokvault.ui.components.GlassIconButton
import com.example.byokvault.ui.components.GradientBackground
import com.example.byokvault.ui.components.PlatformIcon
import kotlinx.coroutines.delay

/**
 * Главный экран приложения со списком платформ
 * Обновлено для соответствия iOS версии с GlassCard и сеткой
 */
@Composable
fun MainScreen(
    onNavigateToAddKey: (platformId: Long?) -> Unit,
    onNavigateToKeyDetail: (keyId: Long) -> Unit,
    onNavigateToPlatformKeys: (platformId: Long) -> Unit,
    viewModel: MainViewModel = viewModel(
        factory = MainViewModel.provideFactory(
            repository = (LocalContext.current.applicationContext as KeyVaultApplication).repository
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = isSystemInDarkTheme()
    
    // Состояние для анимации появления
    var appearAnimation by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        appearAnimation = true
    }

    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Заголовок и кнопка "+"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "API Keys",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color.Black
                )
                
                GlassIconButton(
                    onClick = { onNavigateToAddKey(null) },
                    size = 40.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Добавить ключ",
                        tint = if (isDark) Color.White else Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Контент
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    uiState.platformsWithKeys.isEmpty() -> {
                        EmptyStateView(
                            modifier = Modifier.align(Alignment.Center),
                            onAddClick = { onNavigateToAddKey(null) }
                        )
                    }
                    else -> {
                        PlatformsGrid(
                            platformsWithKeys = uiState.platformsWithKeys,
                            appearAnimation = appearAnimation,
                            onPlatformClick = { platformWithKeys ->
                                if (platformWithKeys.apiKeys.size == 1) {
                                    onNavigateToKeyDetail(platformWithKeys.apiKeys.first().id)
                                } else {
                                    onNavigateToPlatformKeys(platformWithKeys.platform.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Сетка платформ 2 колонки с GlassCard
 */
@Composable
private fun PlatformsGrid(
    platformsWithKeys: List<PlatformWithKeys>,
    appearAnimation: Boolean,
    onPlatformClick: (PlatformWithKeys) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(
            items = platformsWithKeys,
            key = { _, item -> item.platform.id }
        ) { index, platformWithKeys ->
            // Анимация появления с задержкой
            AnimatedVisibility(
                visible = appearAnimation,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = 300,
                        delayMillis = index * 80
                    )
                ) + slideInVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    initialOffsetY = { it / 2 }
                )
            ) {
                PlatformGlassCard(
                    platformWithKeys = platformWithKeys,
                    onClick = { onPlatformClick(platformWithKeys) }
                )
            }
        }
    }
}

/**
 * Карточка платформы в стиле Glass
 */
@Composable
private fun PlatformGlassCard(
    platformWithKeys: PlatformWithKeys,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    
    GlassCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Иконка платформы
            PlatformIcon(
                platform = platformWithKeys.platform,
                size = 56.dp
            )
            
            // Название и количество ключей
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = platformWithKeys.platform.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color.White else Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "${platformWithKeys.apiKeys.size} ${getRussianKeyWord(platformWithKeys.apiKeys.size)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Пустое состояние (нет ключей) - как в iOS
 */
@Composable
private fun EmptyStateView(
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Иконка в стеклянном круге
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    if (isDark) Color.White.copy(alpha = 0.1f)
                    else Color.White.copy(alpha = 0.6f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Key,
                contentDescription = null,
                modifier = Modifier.size(50.dp),
                tint = Color.Transparent
            )
            // Градиентная иконка
            Icon(
                imageVector = Icons.Default.Key,
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFAF52DE), // Purple
                                Color(0xFF007AFF)  // Blue
                            )
                        ),
                        shape = CircleShape
                    )
            )
            // Простая иконка поверх (для видимости)
            Icon(
                imageVector = Icons.Default.Key,
                contentDescription = null,
                modifier = Modifier.size(50.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Нет API ключей",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) Color.White else Color.Black
            )
            
            Text(
                text = "Добавьте первый ключ",
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f)
            )
        }
        
        // Кнопка добавления с градиентом
        Button(
            onClick = onAddClick,
            modifier = Modifier.height(48.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFAF52DE), // Purple
                                Color(0xFF007AFF)  // Blue
                            )
                        ),
                        shape = MaterialTheme.shapes.extraLarge
                    )
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Добавить ключ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Возвращает правильное склонение слова "ключ"
 */
fun getRussianKeyWord(count: Int): String {
    return when {
        count % 10 == 1 && count % 100 != 11 -> "ключ"
        count % 10 in 2..4 && count % 100 !in 12..14 -> "ключа"
        else -> "ключей"
    }
}
