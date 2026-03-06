package com.financasdacasa.app.ui.screens.recurring

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.composables.icons.lucide.*
import com.financasdacasa.app.R
import com.financasdacasa.app.data.model.RecurringTransaction
import com.financasdacasa.app.util.formatAmountFromDigits
import com.financasdacasa.app.util.formatCurrency
import com.financasdacasa.app.util.getLucideIcon
import java.math.BigDecimal
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringScreen(
    onBack: () -> Unit,
    viewModel: RecurringViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(state.error) {
        val msg = when (state.error) {
            "LOAD_FAILED" -> context.getString(R.string.error_unknown)
            "SAVE_FAILED" -> context.getString(R.string.error_recurring_save_failed)
            "DELETE_FAILED" -> context.getString(R.string.error_recurring_delete_failed)
            else -> null
        }
        if (msg != null) snackbarHostState.showSnackbar(msg)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.recurring_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Lucide.ArrowLeft, contentDescription = null)
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.items.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(top = 64.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    Lucide.Repeat,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    stringResource(R.string.no_recurring),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.items, key = { it.id }) { item ->
                    RecurringCard(
                        item = item,
                        onClick = { if (item.isActive) viewModel.showEditForm(item) },
                        onDelete = { viewModel.showDeleteConfirm(item) },
                    )
                }
            }
        }
    }

    // Edit sheet
    if (state.editingItem != null) {
        EditRecurringSheet(
            state = state,
            onCategoryChange = viewModel::onFormCategoryChange,
            onAmountChange = viewModel::onFormAmountChange,
            onDescriptionChange = viewModel::onFormDescriptionChange,
            onSave = viewModel::saveEdit,
            onDismiss = viewModel::dismissEditForm,
        )
    }

    // Delete confirmation
    if (state.deletingItem != null) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDelete,
            title = { Text(stringResource(R.string.delete_recurring_confirm_title)) },
            text = { Text(stringResource(R.string.delete_recurring_confirm_description)) },
            confirmButton = {
                TextButton(onClick = viewModel::deleteRecurring) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDelete) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun RecurringCard(
    item: RecurringTransaction,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val category = item.category
    val color = try {
        Color(android.graphics.Color.parseColor(category?.color ?: "#6366F1"))
    } catch (_: Exception) { Color(0xFF6366F1) }

    // Compute elapsed months
    val startDate = try { LocalDate.parse(item.startDate) } catch (_: Exception) { LocalDate.now() }
    val elapsed = ChronoUnit.MONTHS.between(startDate, LocalDate.now()).toInt().coerceIn(0, item.occurrences)
    val progressFraction = elapsed.toFloat() / item.occurrences

    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = item.isActive) { onClick() },
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (category != null) {
                        Surface(
                            shape = CircleShape,
                            color = color.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(getLucideIcon(category.icon), null, tint = color, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                    Column {
                        Text(item.description, style = MaterialTheme.typography.bodyLarge)
                        if (category != null) {
                            Text(category.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        formatCurrency(BigDecimal(item.amount)),
                        style = MaterialTheme.typography.titleSmall,
                        color = if (item.type == "expense") Color(0xFFF43F5E) else Color(0xFF10B981),
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (item.isActive) Color(0xFF10B981).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Text(
                            stringResource(if (item.isActive) R.string.recurring_active else R.string.recurring_cancelled),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Progress
            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.recurring_progress, elapsed, item.occurrences),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Delete button for active items
            if (item.isActive) {
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDelete) {
                        Icon(Lucide.Trash2, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditRecurringSheet(
    state: RecurringUiState,
    onCategoryChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    val item = state.editingItem ?: return
    val typeCategories = state.categories.filter { it.type == item.type }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(R.string.edit_recurring), style = MaterialTheme.typography.titleLarge)

            // Category picker
            Text(stringResource(R.string.select_category), style = MaterialTheme.typography.labelLarge)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (cat in typeCategories) {
                    val isSelected = cat.id == state.formCategoryId
                    val color = try {
                        Color(android.graphics.Color.parseColor(cat.color))
                    } catch (_: Exception) { Color(0xFF6366F1) }
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth().clickable { onCategoryChange(cat.id) },
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Surface(shape = CircleShape, color = color.copy(alpha = 0.15f), modifier = Modifier.size(32.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(getLucideIcon(cat.icon), null, tint = color, modifier = Modifier.size(16.dp))
                                }
                            }
                            Text(cat.name, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // Amount
            OutlinedTextField(
                value = if (state.formAmountDigits.isNotBlank()) formatAmountFromDigits(state.formAmountDigits) else "",
                onValueChange = { onAmountChange(it) },
                label = { Text(stringResource(R.string.amount)) },
                prefix = { Text("R$") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            // Description
            OutlinedTextField(
                value = state.formDescription,
                onValueChange = onDescriptionChange,
                label = { Text(stringResource(R.string.description)) },
                supportingText = { Text("${state.formDescription.length}/50") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            // Save
            Button(onClick = onSave, modifier = Modifier.fillMaxWidth(), enabled = !state.isSaving) {
                if (state.isSaving) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text(stringResource(R.string.save))
            }
        }
    }
}
