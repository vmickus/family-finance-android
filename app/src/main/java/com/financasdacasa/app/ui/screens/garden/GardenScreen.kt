package com.financasdacasa.app.ui.screens.garden

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Droplets
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Sprout
import com.financasdacasa.app.R
import com.financasdacasa.app.data.model.Goal
import com.financasdacasa.app.util.*
import java.math.BigDecimal
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GardenScreen(
    onGoalClick: (String) -> Unit,
    viewModel: GardenViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(state.error) {
        val msg = when (state.error) {
            "LOAD_FAILED" -> context.getString(R.string.error_unknown)
            "DELETE_FAILED" -> context.getString(R.string.error_goal_delete_failed)
            "SAVE_FAILED" -> context.getString(R.string.error_goal_save_failed)
            else -> null
        }
        if (msg != null) snackbarHostState.showSnackbar(msg)
    }

    LaunchedEffect(state.successMessage) {
        if (state.successMessage == "WATERED") {
            snackbarHostState.showSnackbar(context.getString(R.string.watered_successfully))
            viewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showCreateForm() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Lucide.Plus, contentDescription = stringResource(R.string.create_goal))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Free balance card
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(
                                    stringResource(R.string.free_balance),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    formatCurrency(BigDecimal.valueOf(viewModel.freeBalance)),
                                    style = MaterialTheme.typography.headlineSmall,
                                )
                            }
                            if (viewModel.freeBalance > 0 && viewModel.activeGoals.isNotEmpty()) {
                                FilledTonalButton(onClick = { viewModel.showWaterSheet() }) {
                                    Icon(Lucide.Droplets, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(stringResource(R.string.water_garden))
                                }
                            }
                        }
                    }
                }

                if (state.goals.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                Lucide.Sprout,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                stringResource(R.string.no_goals),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                } else {
                    items(state.goals, key = { it.id }) { goal ->
                        GoalCard(goal = goal, onClick = { onGoalClick(goal.id) })
                    }
                }

                item { Spacer(Modifier.height(72.dp)) }
            }
        }
    }

    // Goal form
    if (state.showGoalForm) {
        GoalFormSheet(
            state = state,
            availablePriority = viewModel.availablePriority,
            onNameChange = viewModel::onFormNameChange,
            onTargetChange = viewModel::onFormTargetChange,
            onPlantTypeChange = viewModel::onFormPlantTypeChange,
            onColorChange = viewModel::onFormColorChange,
            onPriorityChange = viewModel::onFormPriorityChange,
            onDeadlineChange = viewModel::onFormDeadlineChange,
            onSave = viewModel::saveGoal,
            onDismiss = viewModel::dismissGoalForm,
        )
    }

    // Water sheet
    if (state.showWaterSheet) {
        WaterGardenSheet(
            state = state,
            activeGoals = viewModel.activeGoals,
            freeBalance = viewModel.freeBalance,
            onAmountChange = viewModel::onWaterAmountChange,
            onDescriptionChange = viewModel::onWaterDescriptionChange,
            onSubmit = viewModel::submitWater,
            onDismiss = viewModel::dismissWaterSheet,
        )
    }

    // Delete confirmation
    if (state.deletingGoal != null) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDelete,
            title = { Text(stringResource(R.string.delete_goal_title)) },
            text = { Text(stringResource(R.string.delete_goal_description)) },
            confirmButton = {
                TextButton(onClick = viewModel::deleteGoal) {
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
private fun GoalCard(goal: Goal, onClick: () -> Unit) {
    val target = goal.targetAmount.toDoubleOrNull() ?: 1.0
    val current = goal.currentAmount.toDoubleOrNull() ?: 0.0
    val progress = if (target > 0) (current / target).coerceIn(0.0, 1.0) else 0.0
    val color = try {
        Color(android.graphics.Color.parseColor(goal.color))
    } catch (_: Exception) { Color(0xFF5B8A72) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Plant image
            androidx.compose.foundation.Image(
                painter = painterResource(getPlantDrawable(goal.plantType, progress)),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(goal.name, style = MaterialTheme.typography.titleSmall)
                    if (goal.status == "completed") {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.15f),
                        ) {
                            Text(
                                stringResource(R.string.goal_completed),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF10B981),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = color,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        formatCurrency(BigDecimal.valueOf(current)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        formatCurrency(BigDecimal.valueOf(target)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalFormSheet(
    state: GardenUiState,
    availablePriority: Int,
    onNameChange: (String) -> Unit,
    onTargetChange: (String) -> Unit,
    onPlantTypeChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onPriorityChange: (Int) -> Unit,
    onDeadlineChange: (String?) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    val isEditing = state.editingGoal != null
    val context = LocalContext.current
    val selectedColor = try {
        Color(android.graphics.Color.parseColor(state.formColor))
    } catch (_: Exception) { Color(0xFF5B8A72) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                stringResource(if (isEditing) R.string.edit_goal else R.string.create_goal),
                style = MaterialTheme.typography.titleLarge,
            )

            // Plant preview
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                androidx.compose.foundation.Image(
                    painter = painterResource(getPlantDrawable(state.formPlantType, 0.5)),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                )
            }

            // Plant type picker
            Text(stringResource(R.string.plant_type), style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (type in PLANT_TYPE_OPTIONS) {
                    val label = when (type) {
                        "tree" -> stringResource(R.string.plant_tree)
                        "sunflower" -> stringResource(R.string.plant_sunflower)
                        "bonsai" -> stringResource(R.string.plant_bonsai)
                        "cactus" -> stringResource(R.string.plant_cactus)
                        else -> type
                    }
                    val isSelected = type == state.formPlantType
                    FilterChip(
                        selected = isSelected,
                        onClick = { onPlantTypeChange(type) },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // Color picker
            Text(stringResource(R.string.goal_color), style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(GOAL_COLOR_OPTIONS.size) { index ->
                    val hex = GOAL_COLOR_OPTIONS[index]
                    val color = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { Color(0xFF5B8A72) }
                    val isSel = hex == state.formColor
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .then(if (isSel) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier)
                            .clickable { onColorChange(hex) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isSel) Icon(Lucide.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Name
            OutlinedTextField(
                value = state.formName,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.goal_name)) },
                placeholder = { Text(stringResource(R.string.goal_name_hint)) },
                singleLine = true,
                isError = state.formError == "NAME_REQUIRED" || state.formError == "NAME_MAX",
                supportingText = when (state.formError) {
                    "NAME_REQUIRED" -> ({ Text(stringResource(R.string.error_goal_name_required)) })
                    "NAME_MAX" -> ({ Text(stringResource(R.string.error_goal_name_max)) })
                    else -> null
                },
                modifier = Modifier.fillMaxWidth(),
            )

            // Target amount
            OutlinedTextField(
                value = if (state.formTargetDigits.isNotBlank()) formatAmountFromDigits(state.formTargetDigits) else "",
                onValueChange = { onTargetChange(it) },
                label = { Text(stringResource(R.string.target_amount)) },
                prefix = { Text("R$") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = state.formError == "TARGET_REQUIRED",
                supportingText = if (state.formError == "TARGET_REQUIRED") ({ Text(stringResource(R.string.error_target_required)) }) else null,
                modifier = Modifier.fillMaxWidth(),
            )

            // Priority
            Text(
                "${stringResource(R.string.priority_percent)}: ${state.formPriority}% (max $availablePriority%)",
                style = MaterialTheme.typography.labelLarge,
            )
            Slider(
                value = state.formPriority.toFloat(),
                onValueChange = { onPriorityChange(it.toInt()) },
                valueRange = 0f..availablePriority.toFloat().coerceAtLeast(0f),
                steps = if (availablePriority > 0) (availablePriority / 5).coerceAtLeast(1) - 1 else 0,
                modifier = Modifier.fillMaxWidth(),
            )

            // Deadline
            val deadlineText = state.formDeadline ?: stringResource(R.string.deadline_optional)
            OutlinedButton(
                onClick = {
                    val now = LocalDate.now()
                    DatePickerDialog(context, { _, y, m, d ->
                        onDeadlineChange(LocalDate.of(y, m + 1, d).toString())
                    }, now.year, now.monthValue - 1, now.dayOfMonth).show()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("${stringResource(R.string.deadline)}: $deadlineText")
            }

            // Error
            if (state.formError == "SAVE_FAILED") {
                Text(stringResource(R.string.error_goal_save_failed), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            // Save
            Button(onClick = onSave, modifier = Modifier.fillMaxWidth(), enabled = !state.isSaving) {
                if (state.isSaving) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text(stringResource(R.string.save))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WaterGardenSheet(
    state: GardenUiState,
    activeGoals: List<Goal>,
    freeBalance: Double,
    onAmountChange: (String, String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.water_garden), style = MaterialTheme.typography.titleLarge)

            // Free balance
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(stringResource(R.string.free_balance), style = MaterialTheme.typography.labelMedium)
                    Text(formatCurrency(BigDecimal.valueOf(freeBalance)), style = MaterialTheme.typography.labelMedium)
                }
            }

            // Goal amount inputs
            for (goal in activeGoals) {
                val digits = state.waterAmounts[goal.id] ?: ""
                val color = try { Color(android.graphics.Color.parseColor(goal.color)) } catch (_: Exception) { Color(0xFF5B8A72) }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(getPlantDrawable(goal.plantType, 0.5)),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(goal.name, style = MaterialTheme.typography.bodySmall)
                    }
                    OutlinedTextField(
                        value = if (digits.isNotBlank()) formatAmountFromDigits(digits) else "",
                        onValueChange = { onAmountChange(goal.id, it) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.width(120.dp),
                        textStyle = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            // Description
            OutlinedTextField(
                value = state.waterDescription,
                onValueChange = onDescriptionChange,
                label = { Text(stringResource(R.string.allocation_description)) },
                placeholder = { Text(stringResource(R.string.allocation_description_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            // Total
            val total = state.waterAmounts.values.sumOf { (it.toLongOrNull() ?: 0L) / 100.0 }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Total", style = MaterialTheme.typography.titleSmall)
                Text(
                    formatCurrency(BigDecimal.valueOf(total)),
                    style = MaterialTheme.typography.titleSmall,
                    color = if (total > freeBalance + 0.05) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                )
            }

            // Error
            state.waterError?.let { err ->
                val msg = when (err) {
                    "EXCEEDS_BALANCE" -> stringResource(R.string.error_exceeds_balance)
                    "ALLOCATION_FAILED" -> stringResource(R.string.error_allocation_failed)
                    else -> stringResource(R.string.error_unknown)
                }
                Text(msg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            // Submit
            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isWatering && total > 0 && total <= freeBalance + 0.05,
            ) {
                if (state.isWatering) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else {
                    Icon(Lucide.Droplets, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.water_garden))
                }
            }
        }
    }
}
