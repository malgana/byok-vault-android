package com.example.byokvault.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.byokvault.KeyVaultApplication
import com.example.byokvault.data.model.PlatformWithKeys
import com.example.byokvault.ui.components.PlatformIcon

/**
 * Главный экран приложения со списком платформ
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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Заголовок и кнопка "+"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "API Keys",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = { onNavigateToAddKey(null) },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить ключ")
                }
            }

            // Контент
            Box(
                modifier = Modifier.weight(1f)
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    uiState.platformsWithKeys.isEmpty() -> {
                        EmptyStateView(modifier = Modifier.align(Alignment.Center))
                    }
                    else -> {
                        PlatformsList(
                            platformsWithKeys = uiState.platformsWithKeys,
                            onPlatformClick = {
                                if (it.apiKeys.size == 1) {
                                    onNavigateToKeyDetail(it.apiKeys.first().id)
                                } else {
                                    onNavigateToPlatformKeys(it.platform.id)
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
 * Список платформ в виде карточки
 */
@Composable
private fun PlatformsList(
    platformsWithKeys: List<PlatformWithKeys>,
    onPlatformClick: (PlatformWithKeys) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(0.dp)
        ) {
            items(platformsWithKeys.size) { index ->
                val platform = platformsWithKeys[index]
                PlatformRow(platform = platform, onClick = { onPlatformClick(platform) })
                if (index < platformsWithKeys.size - 1) {
                    Divider(modifier = Modifier.padding(start = 72.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                }
            }
        }
    }
}

/**
 * Строка с платформой и количеством ключей
 */
@Composable
private fun PlatformRow(
    platform: PlatformWithKeys,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PlatformIcon(platform = platform.platform, size = 48.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = platform.platform.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${platform.apiKeys.size} ${getRussianKeyWord(platform.apiKeys.size)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

/**
 * Пустое состояние (нет ключей)
 */
@Composable
private fun EmptyStateView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Key,
            contentDescription = null,
            modifier = Modifier.height(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Нет API ключей",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Начните, добавив свой первый API-ключ, нажав на кнопку «+»",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
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
