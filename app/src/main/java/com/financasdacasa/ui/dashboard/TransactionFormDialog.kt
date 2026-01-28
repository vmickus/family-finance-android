package com.financasdacasa.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.financasdacasa.data.model.Category
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFormDialog(
    categories: List<Category>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onCreateTransaction: (
        categoryId: String,
        type: String,
        amount: Double,
        description: String,
        photoPath: String,
        transactionDate: String
    ) -> Unit,
    onCreateCategory: (name: String, color: String) -> Unit
) {
    var type by remember { mutableStateOf("expense") }
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nova Transação") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Type selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = type == "expense",
                        onClick = { type = "expense" },
                        label = { Text("Gasto") },
                        leadingIcon = if (type == "expense") {
                            { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = type == "income",
                        onClick = { type = "income" },
                        label = { Text("Recebimento") },
                        leadingIcon = if (type == "income") {
                            { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Amount
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Valor (R\$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Category
                Text("Categoria", style = MaterialTheme.typography.labelMedium)
                
                if (categories.isEmpty()) {
                    OutlinedButton(
                        onClick = { showCategoryDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Criar primeira categoria")
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        var expanded by remember { mutableStateOf(false) }
                        
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedCategory?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Selecione") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                categories.forEach { category ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(12.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            try {
                                                                Color(android.graphics.Color.parseColor(category.color))
                                                            } catch (e: Exception) {
                                                                Color(0xFF6366F1)
                                                            }
                                                        )
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Text(category.name)
                                            }
                                        },
                                        onClick = {
                                            selectedCategory = category
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        IconButton(onClick = { showCategoryDialog = true }) {
                            Icon(Icons.Default.Add, "Nova categoria")
                        }
                    }
                }

                // Date
                OutlinedTextField(
                    value = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    onValueChange = {},
                    label = { Text("Data") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        Icon(Icons.Default.DateRange, null)
                    }
                )

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedCategory?.let { cat ->
                        onCreateTransaction(
                            cat.id,
                            type,
                            amount.toDoubleOrNull() ?: 0.0,
                            description,
                            "",
                            date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                        )
                    }
                },
                enabled = !isLoading && selectedCategory != null && amount.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Salvar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )

    // New Category Dialog
    if (showCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text("Nova Categoria") },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("Nome") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCategoryName.isNotBlank()) {
                            onCreateCategory(newCategoryName, "#6366F1")
                            newCategoryName = ""
                            showCategoryDialog = false
                        }
                    }
                ) {
                    Text("Criar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCategoryDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
