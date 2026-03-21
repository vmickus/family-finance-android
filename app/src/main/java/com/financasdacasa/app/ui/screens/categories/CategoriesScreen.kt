package com.financasdacasa.app.ui.screens.categories

import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Trash2
import com.financasdacasa.app.R
import com.financasdacasa.app.util.CATEGORY_COLOR_OPTIONS
import com.financasdacasa.app.util.CATEGORY_ICON_OPTIONS
import com.financasdacasa.app.util.getLucideIcon
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onBack: () -> Unit,
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Show error snackbar
    LaunchedEffect(state.error) {
        val msg = when (state.error) {
            "LOAD_FAILED" -> context.getString(R.string.error_unknown)
            "DELETE_FAILED" -> context.getString(R.string.error_category_delete_failed)
            "REORDER_FAILED" -> context.getString(R.string.error_reorder_failed)
            else -> null
        }
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.categories)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Lucide.ArrowLeft, contentDescription = null)
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showCreateForm() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Lucide.Plus, contentDescription = stringResource(R.string.new_category))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Type toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = state.selectedType == "expense",
                    onClick = { viewModel.onTypeChange("expense") },
                    label = { Text(stringResource(R.string.categories_expenses)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFF43F5E).copy(alpha = 0.15f),
                    ),
                    modifier = Modifier.weight(1f),
                )
                FilterChip(
                    selected = state.selectedType == "income",
                    onClick = { viewModel.onTypeChange("income") },
                    label = { Text(stringResource(R.string.categories_income)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF059669).copy(alpha = 0.15f),
                    ),
                    modifier = Modifier.weight(1f),
                )
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                // Reorderable list
                val lazyListState = rememberLazyListState()
                val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
                    viewModel.onReorder(from.index, to.index)
                }

                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp),
                ) {
                    items(state.categories, key = { it.id }) { category ->
                        ReorderableItem(reorderableState, key = category.id) { isDragging ->
                            val elevation = if (isDragging) 8.dp else 0.dp
                            val vibrator = context.getSystemService(Vibrator::class.java)

                            LaunchedEffect(isDragging) {
                                if (isDragging && vibrator != null) {
                                    vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
                                }
                            }

                            Surface(
                                shadowElevation = elevation,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .longPressDraggableHandle(
                                        onDragStopped = { viewModel.commitReorder() },
                                    )
                                    .clickable { viewModel.showEditForm(category) },
                            ) {
                                val color = try {
                                    Color(android.graphics.Color.parseColor(category.color))
                                } catch (_: Exception) {
                                    Color(0xFF6366F1)
                                }
                                val icon = getLucideIcon(category.icon)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = color.copy(alpha = 0.15f),
                                        modifier = Modifier.size(40.dp),
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                    Text(
                                        category.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit/Create bottom sheet
    if (state.showForm) {
        CategoryFormSheet(
            state = state,
            onNameChange = viewModel::onFormNameChange,
            onIconChange = viewModel::onFormIconChange,
            onColorChange = viewModel::onFormColorChange,
            onSave = viewModel::saveCategory,
            onDelete = {
                state.editingCategory?.let {
                    viewModel.dismissForm()
                    viewModel.showDeleteConfirm(it)
                }
            },
            onDismiss = viewModel::dismissForm,
        )
    }

    // Delete confirmation
    if (state.deletingCategory != null) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDelete,
            title = { Text(stringResource(R.string.delete_category_title)) },
            text = { Text(stringResource(R.string.delete_category_description)) },
            confirmButton = {
                TextButton(onClick = viewModel::deleteCategory) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFormSheet(
    state: CategoriesUiState,
    onNameChange: (String) -> Unit,
    onIconChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val isEditing = state.editingCategory != null
    val selectedColor = try {
        Color(android.graphics.Color.parseColor(state.formColor))
    } catch (_: Exception) {
        Color(0xFF8B82B8)
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Title row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(if (isEditing) R.string.edit_category else R.string.new_category),
                    style = MaterialTheme.typography.titleLarge,
                )
                if (isEditing) {
                    IconButton(onClick = onDelete) {
                        Icon(Lucide.Trash2, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // Name
            OutlinedTextField(
                value = state.formName,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.category_name)) },
                placeholder = { Text(stringResource(R.string.category_name_hint)) },
                singleLine = true,
                isError = state.formError == "NAME_REQUIRED" || state.formError == "NAME_MAX",
                supportingText = when (state.formError) {
                    "NAME_REQUIRED" -> ({ Text(stringResource(R.string.error_category_name_required)) })
                    "NAME_MAX" -> ({ Text(stringResource(R.string.error_category_name_max)) })
                    else -> null
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth(),
            )

            // Color picker
            Text(stringResource(R.string.category_color), style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(CATEGORY_COLOR_OPTIONS.size) { index ->
                    val hex = CATEGORY_COLOR_OPTIONS[index]
                    val color = try {
                        Color(android.graphics.Color.parseColor(hex))
                    } catch (_: Exception) {
                        Color(0xFF8B82B8)
                    }
                    val isSelected = hex == state.formColor

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .then(
                                if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                else Modifier,
                            )
                            .clickable { onColorChange(hex) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isSelected) {
                            Icon(Lucide.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Icon picker
            Text(stringResource(R.string.category_icon), style = MaterialTheme.typography.labelLarge)
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                modifier = Modifier.heightIn(max = 240.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(CATEGORY_ICON_OPTIONS) { iconKey ->
                    val icon = getLucideIcon(iconKey)
                    val isSelected = iconKey == state.formIcon

                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) selectedColor.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .size(44.dp)
                            .then(
                                if (isSelected) Modifier.border(2.dp, selectedColor, CircleShape)
                                else Modifier,
                            )
                            .clickable { onIconChange(iconKey) },
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = if (isSelected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }

            // Error message for save failure
            if (state.formError == "SAVE_FAILED") {
                Text(
                    stringResource(R.string.error_category_save_failed),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            // Save button
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSaving,
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.save))
                }
            }
        }
    }
}
