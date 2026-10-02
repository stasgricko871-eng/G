package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ButtonShape
import com.example.data.model.ButtonStyle
import com.example.data.model.VirtualButtonConfig
import com.example.data.model.VirtualKey

@Composable
fun KeySelectionDialog(
    currentKeyId: String,
    onKeySelected: (VirtualKey) -> Unit,
    onDismiss: () -> Unit
) {
    val categories = remember { VirtualKey.KeyCategory.values() }
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    val currentCategory = categories[selectedCategoryIndex]
    val keysForCategory = remember(currentCategory) {
        VirtualKey.ALL_KEYS.filter { it.category == currentCategory }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(0.95f)
                .border(2.dp, Color.White, RoundedCornerShape(8.dp)),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF101014)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "НАЗНАЧЕНИЕ КЛАВИШИ",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TabRow(
                    selectedTabIndex = selectedCategoryIndex,
                    containerColor = Color(0xFF181820),
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedCategoryIndex]),
                            color = Color(0xFF4AE290)
                        )
                    }
                ) {
                    categories.forEachIndexed { index, cat ->
                        Tab(
                            selected = selectedCategoryIndex == index,
                            onClick = { selectedCategoryIndex = index },
                            text = {
                                Text(
                                    text = cat.title,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (selectedCategoryIndex == index) Color(0xFF4AE290) else Color.Gray
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(keysForCategory) { key ->
                        val isSelected = key.id.equals(currentKeyId, ignoreCase = true)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .height(52.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) Color(0xFF4AE290) else Color(0xFF22222C))
                                .border(
                                    1.dp,
                                    if (isSelected) Color.White else Color(0xFF444450),
                                    RoundedCornerShape(4.dp)
                                )
                                .clickable {
                                    onKeySelected(key)
                                    onDismiss()
                                }
                                .padding(4.dp)
                        ) {
                            Text(
                                text = key.displayName,
                                color = if (isSelected) Color.Black else Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF33333E))
                ) {
                    Text("Отмена", color = Color.White, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
fun ButtonEditDialog(
    buttonConfig: VirtualButtonConfig,
    onSave: (VirtualButtonConfig) -> Unit,
    onDuplicate: (VirtualButtonConfig) -> Unit,
    onDelete: (VirtualButtonConfig) -> Unit,
    onDismiss: () -> Unit
) {
    var label by remember { mutableStateOf(buttonConfig.label) }
    var keyId by remember { mutableStateOf(buttonConfig.keyId) }
    var widthDp by remember { mutableIntStateOf(buttonConfig.widthDp) }
    var shape by remember { mutableStateOf(buttonConfig.shape) }
    var style by remember { mutableStateOf(buttonConfig.style) }
    var opacity by remember { mutableStateOf(buttonConfig.opacity) }
    var isDPad by remember { mutableStateOf(buttonConfig.isDPad) }
    var isJoystick by remember { mutableStateOf(buttonConfig.isJoystick) }
    var showKeyPicker by remember { mutableStateOf(false) }

    val mappedKey = remember(keyId) { VirtualKey.findById(keyId) }

    if (showKeyPicker) {
        KeySelectionDialog(
            currentKeyId = keyId,
            onKeySelected = { selectedKey ->
                keyId = selectedKey.id
                if (label.isBlank() || label.length <= 2) {
                    label = selectedKey.id
                }
            },
            onDismiss = { showKeyPicker = false }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(0.95f)
                .border(2.dp, Color.White, RoundedCornerShape(8.dp)),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF101014)
        ) {
            LazyColumn(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "НАСТРОЙКА ЭЛЕМЕНТА",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = Color.White)
                        }
                    }
                }

                // Control Type: Button, Arrows (D-Pad), Joystick
                item {
                    Column {
                        Text("Тип элемента:", color = Color.Gray, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val isBtn = !isDPad && !isJoystick
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isBtn) Color(0xFF4AE290) else Color(0xFF22222E))
                                    .border(1.dp, if (isBtn) Color.White else Color.Gray, RoundedCornerShape(4.dp))
                                    .clickable {
                                        isDPad = false
                                        isJoystick = false
                                        if (widthDp > 90) widthDp = 66
                                    }
                                    .padding(vertical = 8.dp)
                            ) {
                                Text("Кнопка", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = if (isBtn) Color.Black else Color.White)
                            }

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isDPad) Color(0xFF4AE290) else Color(0xFF22222E))
                                    .border(1.dp, if (isDPad) Color.White else Color.Gray, RoundedCornerShape(4.dp))
                                    .clickable {
                                        isDPad = true
                                        isJoystick = false
                                        if (widthDp < 100) widthDp = 145
                                    }
                                    .padding(vertical = 8.dp)
                            ) {
                                Text("Стрелочки", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = if (isDPad) Color.Black else Color.White)
                            }

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isJoystick) Color(0xFF4AE290) else Color(0xFF22222E))
                                    .border(1.dp, if (isJoystick) Color.White else Color.Gray, RoundedCornerShape(4.dp))
                                    .clickable {
                                        isDPad = false
                                        isJoystick = true
                                        if (widthDp < 100) widthDp = 145
                                    }
                                    .padding(vertical = 8.dp)
                            ) {
                                Text("Джойстик", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = if (isJoystick) Color.Black else Color.White)
                            }
                        }
                    }
                }

                // Name / Label
                item {
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text("Название (Текст)", color = Color.Gray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("button_label_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4AE290),
                            unfocusedBorderColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                // Assigned Key
                item {
                    Column {
                        Text("Назначенная клавиша:", color = Color.Gray, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF22222E))
                                .border(1.dp, Color.White, RoundedCornerShape(4.dp))
                                .clickable { showKeyPicker = true }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = mappedKey.displayName,
                                color = Color(0xFF4AE290),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text("ИЗМЕНИТЬ", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                // Size
                item {
                    Column {
                        Text("Размер: ${widthDp} dp", color = Color.White, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                        Slider(
                            value = widthDp.toFloat(),
                            onValueChange = { widthDp = it.toInt() },
                            valueRange = 40f..160f,
                            steps = 12,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF4AE290),
                                activeTrackColor = Color(0xFF4AE290),
                                inactiveTrackColor = Color.DarkGray
                            )
                        )
                    }
                }

                // Opacity
                item {
                    Column {
                        Text("Прозрачность: ${(opacity * 100).toInt()}%", color = Color.White, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                        Slider(
                            value = opacity,
                            onValueChange = { opacity = it },
                            valueRange = 0.1f..1.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF4AE290),
                                activeTrackColor = Color(0xFF4AE290),
                                inactiveTrackColor = Color.DarkGray
                            )
                        )
                    }
                }

                // Shape
                item {
                    Column {
                        Text("Форма:", color = Color.Gray, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ButtonShape.values().filter { it != ButtonShape.DPAD_CROSS }.forEach { s ->
                                val isSel = shape == s
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isSel) Color(0xFF4AE290) else Color(0xFF22222E))
                                        .border(1.dp, if (isSel) Color.White else Color.Gray, RoundedCornerShape(4.dp))
                                        .clickable { shape = s }
                                        .padding(vertical = 8.dp)
                                ) {
                                    Text(
                                        text = s.displayName,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (isSel) Color.Black else Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // Style
                item {
                    Column {
                        Text("Стиль:", color = Color.Gray, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ButtonStyle.values().forEach { st ->
                                val isSel = style == st
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isSel) Color(0xFF4AE290) else Color(0xFF22222E))
                                        .border(1.dp, if (isSel) Color.White else Color.Gray, RoundedCornerShape(4.dp))
                                        .clickable { style = st }
                                        .padding(vertical = 8.dp)
                                ) {
                                    Text(
                                        text = st.name,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (isSel) Color.Black else Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // Actions: Duplicate, Delete, Save
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onDuplicate(buttonConfig)
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Копия", fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }

                        if (!buttonConfig.isDPad) {
                            Button(
                                onClick = {
                                    onDelete(buttonConfig)
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C))
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Удалить", fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            val defaultLbl = if (isJoystick) "JOY" else if (isDPad) "D-Pad" else "BTN"
                            val updated = buttonConfig.copy(
                                label = label.ifBlank { defaultLbl },
                                keyId = keyId,
                                widthDp = widthDp,
                                heightDp = widthDp,
                                shape = shape,
                                style = style,
                                opacity = opacity,
                                isDPad = isDPad,
                                isJoystick = isJoystick
                            )
                            onSave(updated)
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_button_config"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4AE290))
                    ) {
                        Text("СОХРАНИТЬ", color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileNameDialog(
    initialName: String,
    title: String = "НАЗВАНИЕ ПРОФИЛЯ",
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .border(2.dp, Color.White, RoundedCornerShape(8.dp)),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF101014)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Имя профиля", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4AE290),
                        unfocusedBorderColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("Отмена", fontFamily = FontFamily.Monospace)
                    }

                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onConfirm(name.trim())
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4AE290))
                    ) {
                        Text("ОК", color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

@Composable
fun ImportExportJsonDialog(
    isImport: Boolean,
    initialJson: String = "",
    onImport: (String) -> Unit = {},
    onDismiss: () -> Unit
) {
    var jsonText by remember { mutableStateOf(initialJson) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val clipboardManager = LocalClipboardManager.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(0.95f)
                .border(2.dp, Color.White, RoundedCornerShape(8.dp)),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF101014)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isImport) "ИМПОРТ ПРОФИЛЯ JSON" else "ЭКСПОРТ ПРОФИЛЯ JSON",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = Color.White)
                    }
                }

                OutlinedTextField(
                    value = jsonText,
                    onValueChange = {
                        jsonText = it
                        errorMessage = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    readOnly = !isImport,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color(0xFF80CBC4)
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4AE290),
                        unfocusedBorderColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFFF5252),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!isImport) {
                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(jsonText))
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Скопировать", fontFamily = FontFamily.Monospace)
                        }
                    } else {
                        Button(
                            onClick = {
                                if (jsonText.isBlank()) {
                                    errorMessage = "Вставьте JSON профиля"
                                    return@Button
                                }
                                try {
                                    onImport(jsonText.trim())
                                    onDismiss()
                                } catch (e: Exception) {
                                    errorMessage = "Ошибка парсинга JSON: ${e.localizedMessage}"
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4AE290))
                        ) {
                            Text("Импортировать", color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("Закрыть", fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}
