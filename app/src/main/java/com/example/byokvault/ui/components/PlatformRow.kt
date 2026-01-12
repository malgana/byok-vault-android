package com.example.byokvault.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.byokvault.data.model.PlatformWithKeys

/**
 * Компонент строки платформы в списке
 * Аналог PlatformRow из iOS версии
 * 
 * @param platformWithKeys Платформа с её ключами
 * @param modifier Модификатор
 */
@Composable
fun PlatformRow(
    platformWithKeys: PlatformWithKeys,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        // Иконка платформы
        PlatformIcon(
            platform = platformWithKeys.platform,
            size = 40.dp
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Название и количество ключей
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = platformWithKeys.platform.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = getKeysText(platformWithKeys.apiKeys.size),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Получить правильное склонение для количества ключей
 * (аналог логики из iOS версии)
 */
private fun getKeysText(count: Int): String {
    return when {
        count == 1 -> "1 ключ"
        count in 2..4 -> "$count ключа"
        else -> "$count ключей"
    }
}
