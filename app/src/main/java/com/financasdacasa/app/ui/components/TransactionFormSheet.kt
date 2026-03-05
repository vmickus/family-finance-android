package com.financasdacasa.app.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.financasdacasa.app.R
import com.financasdacasa.app.data.model.Transaction
import com.financasdacasa.app.ui.screens.home.TransactionFormViewModel
import com.financasdacasa.app.util.formatAmountFromDigits
import com.financasdacasa.app.util.formatTransactionDate
import com.financasdacasa.app.util.getLucideIcon
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFormSheet(
    editTransaction: Transaction?,
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
    viewModel: TransactionFormViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(editTransaction) {
        viewModel.initialize(editTransaction)
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            Text(
                stringResource(
                    if (editTransaction != null) R.string.edit_transaction
                    else R.string.new_transaction,
                ),
                style = MaterialTheme.typography.titleLarge,
            )

            Spacer(Modifier.height(16.dp))

            // Type toggle
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.type == "expense",
                    onClick = { viewModel.onTypeChange("expense") },
                    label = { Text(stringResource(R.string.expense)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFF43F5E).copy(alpha = 0.15f),
                    ),
                    modifier = Modifier.weight(1f),
                )
                FilterChip(
                    selected = state.type == "income",
                    onClick = { viewModel.onTypeChange("income") },
                    label = { Text(stringResource(R.string.income)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF10B981).copy(alpha = 0.15f),
                    ),
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(16.dp))

            // Amount
            OutlinedTextField(
                value = if (state.amountDigits.isNotBlank()) {
                    formatAmountFromDigits(state.amountDigits)
                } else {
                    ""
                },
                onValueChange = { viewModel.onAmountChange(it) },
                label = { Text(stringResource(R.string.amount)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(12.dp))

            // Date picker
            val parsedDate = remember(state.date) { LocalDate.parse(state.date) }
            OutlinedButton(
                onClick = {
                    DatePickerDialog(
                        context,
                        { _, y, m, d ->
                            viewModel.onDateChange(LocalDate.of(y, m + 1, d).toString())
                        },
                        parsedDate.year,
                        parsedDate.monthValue - 1,
                        parsedDate.dayOfMonth,
                    ).show()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(formatTransactionDate(state.date, context))
            }

            Spacer(Modifier.height(12.dp))

            // Category
            val selectedCategory = state.categories.find { it.id == state.categoryId }
            OutlinedButton(
                onClick = { viewModel.showCategoryPicker() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (selectedCategory != null) {
                    val color = try {
                        Color(android.graphics.Color.parseColor(selectedCategory.color))
                    } catch (_: Exception) {
                        Color(0xFF6366F1)
                    }
                    Icon(
                        getLucideIcon(selectedCategory.icon),
                        null,
                        tint = color,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(selectedCategory.name)
                } else {
                    Text(stringResource(R.string.select_category))
                }
            }

            Spacer(Modifier.height(12.dp))

            // Description
            OutlinedTextField(
                value = state.description,
                onValueChange = { viewModel.onDescriptionChange(it) },
                label = { Text(stringResource(R.string.description)) },
                supportingText = { Text("${state.description.length}/50") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            // Recurring toggle (create mode only)
            if (editTransaction == null) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(stringResource(R.string.recurring))
                    Switch(
                        checked = state.isRecurring,
                        onCheckedChange = { viewModel.onRecurringToggle(it) },
                    )
                }
                if (state.isRecurring) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.occurrences.toString(),
                        onValueChange = {
                            viewModel.onOccurrencesChange(it.toIntOrNull() ?: 12)
                        },
                        label = { Text(stringResource(R.string.occurrences)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        supportingText = { Text(stringResource(R.string.occurrences_range)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // Error
            state.error?.let { code ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(formErrorStringRes(code)),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(Modifier.height(20.dp))

            // Save button
            Button(
                onClick = { viewModel.save(editTransaction?.id, onSaved) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = !state.isSaving,
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(
                        stringResource(
                            if (editTransaction != null) R.string.save else R.string.create,
                        ),
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    if (state.showCategoryPicker) {
        CategoryPicker(
            categories = state.categories,
            selectedType = state.type,
            selectedId = state.categoryId,
            onSelect = { viewModel.onCategorySelect(it) },
            onDismiss = { viewModel.dismissCategoryPicker() },
        )
    }
}

fun formErrorStringRes(code: String): Int = when (code) {
    "CATEGORY_REQUIRED" -> R.string.error_select_category
    "AMOUNT_REQUIRED" -> R.string.error_amount_positive
    "DESCRIPTION_REQUIRED" -> R.string.error_description_required
    "DESCRIPTION_TOO_LONG" -> R.string.error_description_max
    "SAVE_FAILED" -> R.string.error_save_failed
    else -> R.string.error_unknown
}
