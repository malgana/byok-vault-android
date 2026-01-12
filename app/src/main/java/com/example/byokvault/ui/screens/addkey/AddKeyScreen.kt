package com.example.byokvault.ui.screens.addkey

import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.byokvault.KeyVaultApplication
import com.example.byokvault.utils.ImageHelper

/**
 * Экран добавления/редактирования API ключа
 * Аналог AddKeyView.swift из iOS версии
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddKeyScreen(
    keyId: Long?,
    platformId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: AddKeyViewModel = viewModel(
        factory = AddKeyViewModel.provideFactory(
            repository = (LocalContext.current.applicationContext as KeyVaultApplication).repository,
            keystoreService = (LocalContext.current.applicationContext as KeyVaultApplication).keystoreService,
            editingKeyId = keyId,
            preselectedPlatformId = platformId
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    
    // Launcher для выбора изображения
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.loadIcon(context, it) }
    }
    
    // Показ ошибки
    if (uiState.showError && uiState.errorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissError() },
            title = { Text("Ошибка") },
            text = { Text(uiState.errorMessage!!) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissError() }) {
                    Text("OK")
                }
            }
        )
    }
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (uiState.isEditMode) "Редактировать ключ" else "Новый ключ",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("Отмена", style = MaterialTheme.typography.bodyLarge)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Выбор платформы (только для нового ключа)
                if (!uiState.isEditMode) {
                    PlatformSelector(
                        selectedPlatform = uiState.selectedPlatformName,
                        availablePlatforms = uiState.availablePlatforms,
                        customPlatformName = uiState.customPlatformName,
                        onPlatformSelected = viewModel::selectPlatform,
                        onCustomPlatformNameChanged = viewModel::updateCustomPlatformName
                    )
                    
                    // Загрузка иконки для новой платформы
                    if (uiState.selectedPlatformName == "New" && uiState.customPlatformName.isNotBlank()) {
                        CustomIconPicker(
                            iconData = uiState.customIconData,
                            onPickImage = { imagePickerLauncher.launch("image/*") }
                        )
                    }
                } else {
                    // В режиме редактирования показываем платформу
                    SectionTitle("Платформа")
                    Text(
                        text = uiState.editingPlatform?.name ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                // Название ключа
                SectionTitle("Название")
                OutlinedTextField(
                    value = uiState.myName,
                    onValueChange = viewModel::updateMyName,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Название ключа") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
                
                // Заметка
                SectionTitle("Заметка (опционально)")
                OutlinedTextField(
                    value = uiState.note,
                    onValueChange = viewModel::updateNote,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Добавить заметку...") },
                    minLines = 3,
                    maxLines = 6,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    )
                )
                
                // API ключ
                ApiKeyField(
                    apiKeyValue = uiState.apiKeyValue,
                    onPasteClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clipData = clipboard.primaryClip
                        if (clipData != null && clipData.itemCount > 0) {
                            val text = clipData.getItemAt(0).text?.toString() ?: ""
                            viewModel.pasteFromClipboard(text)
                        }
                    }
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Кнопка сохранения
                Button(
                    onClick = {
                        viewModel.validateAndSave(onSuccess = onNavigateBack)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = viewModel.isFormValid() && !uiState.isSaving,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            text = if (uiState.isEditMode) "Сохранить изменения" else "Сохранить ключ",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Заголовок секции
 */
@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
    )
}

/**
 * Селектор платформы с выпадающим списком
 */
@Composable
private fun PlatformSelector(
    selectedPlatform: String,
    availablePlatforms: List<String>,
    customPlatformName: String,
    onPlatformSelected: (String) -> Unit,
    onCustomPlatformNameChanged: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle("Платформа")
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = if (selectedPlatform.isBlank()) "Выберите платформу" else selectedPlatform,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(16.dp),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = OutlinedTextFieldDefaults.colors()
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                availablePlatforms.forEach { platform ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = if (platform == "New") "НОВАЯ ПЛАТФОРМА" else platform,
                                color = if (platform == "New") {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                fontWeight = if (platform == "New") FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onPlatformSelected(platform)
                            expanded = false
                        }
                    )
                }
            }
        }
        
        // Поле для новой платформы
        if (selectedPlatform == "New") {
            OutlinedTextField(
                value = customPlatformName,
                onValueChange = onCustomPlatformNameChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Название платформы") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

/**
 * Выбор кастомной иконки
 */
@Composable
private fun CustomIconPicker(
    iconData: String?,
    onPickImage: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle("Иконка (опционально)")
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onPickImage)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Превью иконки
            if (iconData != null) {
                val bitmap = ImageHelper.base64ToBitmap(iconData)
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                } else {
                    DefaultIconPlaceholder()
                }
            } else {
                DefaultIconPlaceholder()
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (iconData == null) "Добавить иконку" else "Изменить иконку",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Рекомендуемый размер: 250×250px",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DefaultIconPlaceholder() {
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.PhotoLibrary,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Поле для API ключа с кнопкой вставки
 */
@Composable
private fun ApiKeyField(
    apiKeyValue: String,
    onPasteClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle("API Ключ")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onPasteClick)
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (apiKeyValue.isEmpty()) {
                    Text(
                        text = "Нажмите чтобы вставить ключ",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Text(
                        text = apiKeyValue,
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.ContentPaste,
                    contentDescription = "Вставить из буфера",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
