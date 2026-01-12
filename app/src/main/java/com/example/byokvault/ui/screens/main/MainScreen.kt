package com.example.byokvault.ui.screens.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.byokvault.KeyVaultApplication

/**
 * Главный экран приложения со списком платформ
 * Аналог MainView.swift из iOS версии
 */
@OptIn(ExperimentalMaterial3Api::class)
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
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "API Keys",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToAddKey(null) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Добавить ключ"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // Загрузка
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                // Пустой список
                uiState.platformsWithKeys.isEmpty() -> {
                    EmptyStateView(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                // Список платформ
                else -> {
                    PlatformsList(
                        platformsWithKeys = uiState.platformsWithKeys,
                        onPlatformClick = { platformWithKeys ->
                            // Если у платформы 1 ключ - идем сразу в детали
                            // Иначе - в список ключей платформы
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

/**
 * Список платформ с ключами
 */
@Composable
private fun PlatformsList(
    platformsWithKeys: List<com.example.byokvault.data.model.PlatformWithKeys>,
    onPlatformClick: (com.example.byokvault.data.model.PlatformWithKeys) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = platformsWithKeys,
            key = { it.platform.id }
        ) { platformWithKeys ->
            com.example.byokvault.ui.components.PlatformRow(
                platformWithKeys = platformWithKeys,
                modifier = Modifier
                    .clickable { onPlatformClick(platformWithKeys) }
                    .padding(8.dp)
            )
        }
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
            modifier = Modifier.padding(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = "Нет API ключей",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = "Добавьте первый ключ, нажав на +",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
