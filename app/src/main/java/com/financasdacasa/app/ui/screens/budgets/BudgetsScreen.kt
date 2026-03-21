package com.financasdacasa.app.ui.screens.budgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Target
import com.composables.icons.lucide.Trash2
import com.financasdacasa.app.R
import com.financasdacasa.app.data.model.BudgetLimit
import com.financasdacasa.app.util.formatAmountFromDigits
import com.financasdacasa.app.util.formatCurrency
import com.financasdacasa.app.util.getLucideIcon
import java.math.BigDecimal
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    viewModel: BudgetsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Compute spent by category
    val spentByCategory = remember(state.transactions) {
        val map = mutableMapOf<String, Double>()
        for (tx in state.transactions) {
            if (tx.type == "expense" && tx.categoryId != null) {
                map[tx.categoryId] = (map[tx.categoryId] ?: 0.0) + (tx.amount.toDoubleOrNull() ?: 0.0)
            }
        }
        map
    }

    val totalSpent = remember(state.limits, spentByCategory) {
        state.limits.sumOf { spentByCategory[it.categoryId] ?: 0.0 }
    }
    val totalBudget = remember(state.limits) {
        state.limits.sumOf { it.monthlyLimit.toDoubleOrNull() ?: 0.0 }
    }

    val monthName = remember(state.month) {
        Month.of(state.month).getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault())
            .replaceFirstChar { it.uppercase() }
    }

    Scaffold(
        floatingActionButton = {
            if (viewModel.isCurrentMonth) {
                FloatingActionButton(
                    onClick = { viewModel.showAddForm() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(Lucide.Plus, contentDescription = stringResource(R.string.add_budget_limit))
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            // Month navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = viewModel::previousMonth) {
                    Icon(Lucide.ChevronLeft, contentDescription = null)
                }
                Text(
                    "$monthName ${state.year}",
                    style = MaterialTheme.typography.titleMedium,
                )
                IconButton(onClick = viewModel::nextMonth) {
                    Icon(Lucide.ChevronRight, contentDescription = null)
                }
            }

            Spacer(Modifier.height(8.dp))

            if (state.isLoading) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.limits.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 64.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        Lucide.Target,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        stringResource(R.string.no_budget_limits),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                // Summary header
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            stringResource(R.string.total_budget),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            val totalRatio = if (totalBudget > 0) totalSpent / totalBudget else 0.0
                            Text(
                                formatCurrency(BigDecimal.valueOf(totalSpent)),
                                style = MaterialTheme.typography.headlineSmall,
                                color = if (totalRatio > 1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "/ ${formatCurrency(BigDecimal.valueOf(totalBudget))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        // Progress bar
                        val ratio = if (totalBudget > 0) (totalSpent / totalBudget).coerceAtMost(1.0) else 0.0
                        val totalRatio = if (totalBudget > 0) totalSpent / totalBudget else 0.0
                        LinearProgressIndicator(
                            progress = { ratio.toFloat() },
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = when {
                                totalRatio > 1.0 -> MaterialTheme.colorScheme.error
                                totalRatio > 0.8 -> Color(0xFFF59E0B)
                                else -> MaterialTheme.colorScheme.primary
                            },
                            trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Budget cards grid (non-lazy since inside scrollable)
                val chunked = state.limits.chunked(2)
                for (row in chunked) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        for (bl in row) {
                            BudgetCard(
                                budgetLimit = bl,
                                spent = spentByCategory[bl.categoryId] ?: 0.0,
                                onDelete = { viewModel.showDeleteConfirm(bl.id) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (row.size == 1) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }

    // Add budget bottom sheet
    if (state.showAddForm) {
        AddBudgetSheet(
            state = state,
            spentCategoryIds = state.limits.map { it.categoryId }.toSet(),
            onCategorySelect = viewModel::onCategorySelect,
            onAmountChange = viewModel::onAmountChange,
            onSave = viewModel::saveBudgetLimit,
            onDismiss = viewModel::dismissAddForm,
        )
    }

    // Delete confirmation
    if (state.deletingLimitId != null) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDelete,
            title = { Text(stringResource(R.string.delete_budget_title)) },
            text = { Text(stringResource(R.string.delete_budget_description)) },
            confirmButton = {
                TextButton(onClick = viewModel::deleteBudgetLimit) {
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
private fun BudgetCard(
    budgetLimit: BudgetLimit,
    spent: Double,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val limit = budgetLimit.monthlyLimit.toDoubleOrNull() ?: 0.0
    val ratio = if (limit > 0) spent / limit else 0.0
    val exceeded = ratio > 1
    val category = budgetLimit.category
    val color = try {
        Color(android.graphics.Color.parseColor(category?.color ?: "#6366F1"))
    } catch (_: Exception) {
        Color(0xFF6366F1)
    }
    val icon = getLucideIcon(category?.icon ?: "Tag")

    OutlinedCard(
        modifier = modifier,
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (exceeded) {
                MaterialTheme.colorScheme.error.copy(alpha = 0.05f)
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (exceeded) {
                    MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
            ),
        ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Delete icon
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.align(Alignment.TopEnd).size(28.dp),
                ) {
                    Icon(
                        Lucide.Trash2,
                        contentDescription = stringResource(R.string.delete),
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    )
                }
            }
            // Gauge ring
            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center,
            ) {
                val arcColor = if (exceeded) MaterialTheme.colorScheme.error else color
                val trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)

                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 10.dp.toPx()
                    val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                    drawArc(
                        color = trackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )

                    drawArc(
                        color = arcColor,
                        startAngle = -90f,
                        sweepAngle = (ratio.coerceAtMost(1.0) * 360).toFloat(),
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        formatCurrency(BigDecimal.valueOf(spent)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "/ ${formatCurrency(BigDecimal.valueOf(limit))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Category icon + name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = color.copy(alpha = 0.15f),
                    modifier = Modifier.size(20.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
                    }
                }
                Text(
                    category?.name ?: "",
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                )
            }

            Spacer(Modifier.height(4.dp))

            // Remaining / Exceeded
            if (exceeded) {
                Text(
                    stringResource(R.string.budget_exceeded, formatCurrency(BigDecimal.valueOf(spent - limit))),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                )
            } else {
                Text(
                    stringResource(R.string.budget_remaining, formatCurrency(BigDecimal.valueOf(limit - spent))),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBudgetSheet(
    state: BudgetsUiState,
    spentCategoryIds: Set<String>,
    onCategorySelect: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    val available = state.expenseCategories.filter { it.id !in spentCategoryIds }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                stringResource(R.string.add_budget_limit),
                style = MaterialTheme.typography.titleLarge,
            )

            // Category list
            Text(stringResource(R.string.select_category), style = MaterialTheme.typography.labelLarge)
            Column(
                modifier = Modifier.heightIn(max = 200.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                for (cat in available) {
                    val isSelected = cat.id == state.selectedCategoryId
                    val color = try {
                        Color(android.graphics.Color.parseColor(cat.color))
                    } catch (_: Exception) {
                        Color(0xFF6366F1)
                    }
                    val icon = getLucideIcon(cat.icon)

                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                        border = if (isSelected) {
                            CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
                            )
                        } else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCategorySelect(cat.id) },
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = color.copy(alpha = 0.15f),
                                modifier = Modifier.size(32.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                                }
                            }
                            Text(cat.name, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // Amount input
            Text(stringResource(R.string.monthly_limit), style = MaterialTheme.typography.labelLarge)
            OutlinedTextField(
                value = formatAmountFromDigits(state.amountDigits),
                onValueChange = { onAmountChange(it) },
                prefix = { Text("R$") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            // Save button
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSaving && state.selectedCategoryId.isNotEmpty() && state.amountDigits.isNotEmpty(),
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
