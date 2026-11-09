package com.raymondwmng.dailymedchecker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.raymondwmng.dailymedchecker.R
import com.raymondwmng.dailymedchecker.model.CapsuleSlot
import com.raymondwmng.dailymedchecker.model.DateTimeFormatters

@Composable
fun RestoreCapsuleDialog(
    slot: CapsuleSlot,
    onConfirmRestore: () -> Unit,
    onDismiss: () -> Unit
) {
    val takenTimeText = slot.takenAtMillis?.let { DateTimeFormatters.formatFullDateTime(it) } ?: ""

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.dialog_restore_title, slot.slotNumber),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.dialog_restore_line1),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = takenTimeText,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.dialog_restore_line2, slot.slotNumber),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirmRestore) {
                Text(stringResource(R.string.btn_restore_capsule))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PackSizeSettingsDialog(
    currentPackSize: Int,
    defaultPackSize: Int,
    onSaveSize: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var inputText by remember(defaultPackSize) { mutableStateOf(defaultPackSize.toString()) }
    val parsedSize = inputText.toIntOrNull()
    val isValid = parsedSize != null && parsedSize in 1..60
    val presets = listOf(7, 10, 12, 14, 21, 28, 30)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.dialog_settings_title),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.dialog_settings_desc),
                    style = MaterialTheme.typography.bodyMedium
                )
                if (currentPackSize != defaultPackSize) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(
                            R.string.dialog_settings_active_info,
                            currentPackSize,
                            defaultPackSize
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { value ->
                        if (value.length <= 2 && value.all { it.isDigit() }) {
                            inputText = value
                        }
                    },
                    label = { Text(stringResource(R.string.dialog_settings_field_label)) },
                    singleLine = true,
                    isError = inputText.isNotEmpty() && !isValid,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.dialog_settings_presets),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    presets.forEach { preset ->
                        FilterChip(
                            selected = parsedSize == preset,
                            onClick = { inputText = preset.toString() },
                            label = { Text("$preset") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { parsedSize?.let(onSaveSize) },
                enabled = isValid
            ) {
                Text(stringResource(R.string.btn_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}

@Composable
fun PackSizeConflictDialog(
    newSize: Int,
    takenCount: Int,
    currentPackSize: Int,
    onApplyNextPack: () -> Unit,
    onResetCurrentPack: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.dialog_conflict_title),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(
                        R.string.dialog_conflict_line1,
                        takenCount,
                        currentPackSize
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.dialog_conflict_line2, newSize),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onApplyNextPack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.btn_apply_next_pack, newSize))
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onResetCurrentPack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.btn_archive_start_now))
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        }
    )
}
