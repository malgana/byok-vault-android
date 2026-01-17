package com.example.byokvault.ui.screens.platformkeys

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.byokvault.KeyVaultApplication
import com.example.byokvault.data.model.APIKey
import com.example.byokvault.ui.components.GlassIconButton
import com.example.byokvault.ui.components.GlassListCard
import com.example.byokvault.ui.components.GradientBackground
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

/**
 * Экран списка ключей для конкретной платформы
 * Обновлено для соответствия iOS версии с GlassCard и копированием
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatformKeysListScreen(
    platformId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToKeyDetail: (Long) -> Unit,
    onNavigateToAddKey: (Long) -> Unit,
    viewModel: PlatformKeysListViewModel = viewModel(
        factory = PlatformKeysListViewModel.provideFactory(
            repository = (LocalContext.current.applicationContext as KeyVaultApplication).repository,
            keystoreService = (LocalContext.current.applicationContext as KeyVaultApplication).keystoreService,
            platformId = platformId
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current
    
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
            // Тулбар
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Кнопка назад
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = if (isDark) Color.White else Color.Black
                    )
                }
                
                // Заголовок
                Text(
                    text = uiState.platform?.name ?: "",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color.White else Color.Black,
                    modifier = Modifier.weight(1f)
                )
                
                // Кнопка добавления
                GlassIconButton(
                    onClick = { onNavigateToAddKey(platformId) },
                    size = 36.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Добавить ключ",
                        tint = if (isDark) Color.White else Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // Контент
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(
                        items = uiState.apiKeys,
                        key = { _, item -> item.id }
                    ) { index, apiKey ->
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
                            KeyGlassCard(
                                apiKey = apiKey,
                                isCopied = uiState.copiedKeyId == apiKey.keystoreId,
                                onClick = { onNavigateToKeyDetail(apiKey.id) },
                                onCopyClick = {
                                    // Получаем значение ключа
                                    val keyValue = viewModel.getKeyValue(apiKey.keystoreId)
                                    if (keyValue != null) {
                                        // Копируем в буфер
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("API Key", keyValue)
                                        clipboard.setPrimaryClip(clip)
                                        
                                        // Haptic feedback
                                        triggerHapticFeedback(context)
                                        
                                        // Показываем сообщение
                                        viewModel.showCopiedMessage(apiKey.keystoreId)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Карточка ключа в стиле Glass
 */
@Composable
private fun KeyGlassCard(
    apiKey: APIKey,
    isCopied: Boolean,
    onClick: () -> Unit,
    onCopyClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    
    GlassListCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Индикатор статуса
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        if (apiKey.isValid) Color(0xFF34C759) // Зелёный
                        else Color.Gray.copy(alpha = 0.5f)
                    )
            )
            
            // Информация о ключе
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = apiKey.myName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color.White else Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Заметка (если есть)
                if (!apiKey.note.isNullOrBlank()) {
                    Text(
                        text = apiKey.note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Дата добавления
                Text(
                    text = formatDate(apiKey.dateAdded),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) Color.White.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.4f)
                )
            }
            
            // Кнопка копирования
            IconButton(
                onClick = onCopyClick,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                    contentDescription = if (isCopied) "Скопировано" else "Копировать",
                    tint = if (isCopied) Color(0xFF34C759) else {
                        if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f)
                    },
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

/**
 * Форматирование даты
 */
private fun formatDate(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("d MMM yyyy", Locale("ru", "RU"))
    return dateFormat.format(Date(timestamp))
}

/**
 * Haptic feedback при копировании
 */
private fun triggerHapticFeedback(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(50)
            }
        }
    } catch (_: Exception) {
        // Игнорируем ошибки вибрации
    }
}
