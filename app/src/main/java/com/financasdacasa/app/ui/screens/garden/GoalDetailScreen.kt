package com.financasdacasa.app.ui.screens.garden

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.composables.icons.lucide.*
import com.financasdacasa.app.R
import com.financasdacasa.app.util.formatCurrency
import com.financasdacasa.app.util.formatTransactionDate
import com.financasdacasa.app.util.getPlantDrawable
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailScreen(
    onBack: () -> Unit,
    viewModel: GoalDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val goal = state.goal
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(goal?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Lucide.ArrowLeft, contentDescription = null)
                    }
                },
                actions = {
                    if (goal != null) {
                        IconButton(onClick = { viewModel.showDeleteConfirm() }) {
                            Icon(Lucide.Trash2, null, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
            )
        },
    ) { padding ->
        if (state.isLoading || goal == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val target = goal.targetAmount.toDoubleOrNull() ?: 1.0
            val current = goal.currentAmount.toDoubleOrNull() ?: 0.0
            val progress = if (target > 0) (current / target).coerceIn(0.0, 1.0) else 0.0
            val color = try {
                Color(android.graphics.Color.parseColor(goal.color))
            } catch (_: Exception) { Color(0xFF5B8A72) }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Hero
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        androidx.compose.foundation.Image(
                            painter = painterResource(getPlantDrawable(goal.plantType, progress)),
                            contentDescription = null,
                            modifier = Modifier.size(180.dp),
                        )
                    }
                }

                // Progress card
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    formatCurrency(BigDecimal.valueOf(current)),
                                    style = MaterialTheme.typography.headlineSmall,
                                )
                                Text(
                                    "/ ${formatCurrency(BigDecimal.valueOf(target))}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { progress.toFloat() },
                                modifier = Modifier.fillMaxWidth().height(8.dp),
                                color = color,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    "${(progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                )
                                if (current < target) {
                                    Text(
                                        stringResource(R.string.remaining_to_goal, formatCurrency(BigDecimal.valueOf(target - current))),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }

                // Stats
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (goal.priorityPercent > 0) {
                            SuggestionChip(
                                onClick = {},
                                label = { Text("${stringResource(R.string.priority_percent)}: ${goal.priorityPercent}%") },
                            )
                        }
                        if (goal.deadline != null) {
                            SuggestionChip(
                                onClick = {},
                                label = { Text("${stringResource(R.string.deadline)}: ${formatTransactionDate(goal.deadline, context)}") },
                            )
                        }
                        if (goal.status == "completed") {
                            SuggestionChip(
                                onClick = {},
                                label = { Text(stringResource(R.string.goal_completed)) },
                            )
                        }
                    }
                }

                // Allocation history header
                item {
                    Text(
                        stringResource(R.string.allocation_history),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }

                if (state.allocations.isEmpty()) {
                    item {
                        Text(
                            stringResource(R.string.no_allocations),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    items(state.allocations, key = { it.id }) { alloc ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column {
                                    Text(
                                        formatCurrency(BigDecimal(alloc.amount)),
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                    Text(
                                        formatTransactionDate(alloc.allocationDate, context),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    if (alloc.user != null) {
                                        Text(
                                            alloc.user.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                                IconButton(onClick = { viewModel.deleteAllocation(alloc.id) }) {
                                    Icon(Lucide.Trash2, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    // Delete confirmation
    if (state.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteConfirm,
            title = { Text(stringResource(R.string.delete_goal_title)) },
            text = { Text(stringResource(R.string.delete_goal_description)) },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteGoal(onBack) }) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDeleteConfirm) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}
