package com.financasdacasa.app.ui.screens.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.financasdacasa.app.R
import com.financasdacasa.app.data.model.Transaction
import com.financasdacasa.app.util.formatCurrency
import com.financasdacasa.app.util.getLucideIcon
import com.financasdacasa.app.util.getMonthName
import java.math.BigDecimal

private data class CategoryGroupItem(
    val categoryId: String?,
    val name: String,
    val icon: String,
    val color: Color,
    val total: Double,
)

private fun parseHexColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        Color(0xFF9CA3AF)
    }
}

private fun groupByCategory(
    transactions: List<Transaction>,
    viewType: String,
): List<CategoryGroupItem> {
    val map = mutableMapOf<String, CategoryGroupItem>()
    for (tx in transactions) {
        if (tx.type != viewType) continue
        val name = tx.category?.name ?: "Other"
        val amount = tx.amount.toDoubleOrNull() ?: 0.0
        val existing = map[name]
        if (existing != null) {
            map[name] = existing.copy(total = existing.total + amount)
        } else {
            map[name] = CategoryGroupItem(
                categoryId = tx.categoryId,
                name = name,
                icon = tx.category?.icon ?: "Tag",
                color = parseHexColor(tx.category?.color ?: "#9CA3AF"),
                total = amount,
            )
        }
    }
    return map.values.sortedByDescending { it.total }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpendingByCategoryScreen(
    onBack: () -> Unit,
    onCategoryClick: ((String) -> Unit)? = null,
    viewModel: SpendingByCategoryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var viewType by remember { mutableStateOf("expense") }

    val activeTransactions = if (state.viewMode == DashboardViewMode.ANNUAL) {
        state.annualReport?.transactions ?: emptyList()
    } else {
        state.transactions
    }

    val categories = remember(activeTransactions, viewType) {
        groupByCategory(activeTransactions, viewType)
    }

    val grandTotal = categories.sumOf { it.total }
    val maxTotal = categories.firstOrNull()?.total ?: 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.dashboard_by_category),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            // Period navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { viewModel.previousPeriod() }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = null)
                }
                Row(
                    modifier = Modifier.clickable { viewModel.toggleViewMode() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    val label = if (state.viewMode == DashboardViewMode.ANNUAL) {
                        "${state.year}"
                    } else {
                        "${getMonthName(state.month)} ${state.year}"
                    }
                    Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                IconButton(onClick = { viewModel.nextPeriod() }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }

            // Expense/Income toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .padding(2.dp),
                ) {
                    ToggleChipItem(
                        label = stringResource(R.string.expense),
                        selected = viewType == "expense",
                        selectedColor = Color(0xFFF43F5E),
                        onClick = { viewType = "expense" },
                    )
                    ToggleChipItem(
                        label = stringResource(R.string.income),
                        selected = viewType == "income",
                        selectedColor = Color(0xFF10B981),
                        onClick = { viewType = "income" },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (state.isLoading) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Column
            }

            if (state.error != null) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.dashboard_no_data),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                return@Column
            }

            if (categories.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.dashboard_no_data),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                return@Column
            }

            // Donut chart
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    AllCategoriesDonutChart(
                        categories = categories,
                        modifier = Modifier.size(180.dp),
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            stringResource(R.string.category_total),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            formatCurrency(BigDecimal.valueOf(grandTotal)),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Category list
            categories.forEach { cat ->
                val percentage = if (grandTotal > 0) (cat.total / grandTotal * 100) else 0.0
                AllCategoriesCategoryRow(
                    cat = cat,
                    maxTotal = maxTotal,
                    percentage = percentage,
                    onClick = if (onCategoryClick != null && cat.categoryId != null) {
                        { onCategoryClick(cat.categoryId) }
                    } else null,
                )
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ToggleChipItem(label: String, selected: Boolean, selectedColor: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        color = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = if (selected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AllCategoriesDonutChart(categories: List<CategoryGroupItem>, modifier: Modifier = Modifier) {
    val total = categories.sumOf { it.total }
    Canvas(modifier) {
        val strokeWidth = size.minDimension * 0.12f
        val radius = (size.minDimension - strokeWidth) / 2f
        var startAngle = -90f
        categories.forEach { cat ->
            val sweep = if (total > 0) (cat.total / total * 360f).toFloat() else 0f
            drawArc(
                color = cat.color,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                topLeft = androidx.compose.ui.geometry.Offset(
                    (size.width - radius * 2) / 2f,
                    (size.height - radius * 2) / 2f,
                ),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            )
            startAngle += sweep
        }
    }
}

@Composable
private fun AllCategoriesCategoryRow(
    cat: CategoryGroupItem,
    maxTotal: Double,
    percentage: Double,
    onClick: (() -> Unit)? = null,
) {
    val icon = getLucideIcon(cat.icon)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = if (onClick != null) {
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onClick)
                .padding(vertical = 4.dp)
        } else {
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        },
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(cat.color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = cat.color)
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    cat.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Text(
                    formatCurrency(BigDecimal.valueOf(cat.total)),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp))) {
                    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surfaceVariant) {}
                    Surface(
                        Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = if (maxTotal > 0) (cat.total / maxTotal).toFloat() else 0f),
                        color = cat.color,
                        shape = RoundedCornerShape(3.dp),
                    ) {}
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "%.1f%%".format(percentage),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
