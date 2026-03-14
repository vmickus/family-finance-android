package com.financasdacasa.app.ui.screens.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.financasdacasa.app.R
import com.financasdacasa.app.data.model.Transaction
import com.financasdacasa.app.util.formatCurrency
import com.financasdacasa.app.util.getLucideIcon
import java.math.BigDecimal

private data class CategoryGroup(
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

@Composable
fun SpendingByCategoryCard(
    transactions: List<Transaction>,
    onCategoryClick: ((String) -> Unit)? = null,
    onViewAll: (() -> Unit)? = null,
) {
    var viewType by remember { mutableStateOf("expense") }

    val categories = remember(transactions, viewType) {
        val map = mutableMapOf<String, CategoryGroup>()
        for (tx in transactions) {
            if (tx.type != viewType) continue
            val name = tx.category?.name ?: "Other"
            val amount = tx.amount.toDoubleOrNull() ?: 0.0
            val existing = map[name]
            if (existing != null) {
                map[name] = existing.copy(total = existing.total + amount)
            } else {
                map[name] = CategoryGroup(
                    categoryId = tx.categoryId,
                    name = name,
                    icon = tx.category?.icon ?: "Tag",
                    color = parseHexColor(tx.category?.color ?: "#9CA3AF"),
                    total = amount,
                )
            }
        }
        map.values.sortedByDescending { it.total }.take(5)
    }

    val maxTotal = categories.firstOrNull()?.total ?: 0.0

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(12.dp)) {
            // Header with toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.dashboard_by_category),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(2.dp),
                ) {
                    ToggleChip(
                        label = stringResource(R.string.expense),
                        selected = viewType == "expense",
                        selectedColor = Color(0xFFF43F5E),
                        onClick = { viewType = "expense" },
                    )
                    ToggleChip(
                        label = stringResource(R.string.income),
                        selected = viewType == "income",
                        selectedColor = Color(0xFF10B981),
                        onClick = { viewType = "income" },
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            if (categories.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.dashboard_no_data),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DonutChart(categories = categories, modifier = Modifier.size(120.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        categories.forEach { cat ->
                            CategoryRow(
                                cat = cat,
                                maxTotal = maxTotal,
                                onClick = if (onCategoryClick != null && cat.categoryId != null) {
                                    { onCategoryClick(cat.categoryId) }
                                } else null,
                            )
                        }
                    }
                }

                if (onViewAll != null) {
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        Text(
                            stringResource(R.string.dashboard_view_all),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable(onClick = onViewAll),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ToggleChip(label: String, selected: Boolean, selectedColor: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        color = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = if (selected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DonutChart(categories: List<CategoryGroup>, modifier: Modifier = Modifier) {
    val total = categories.sumOf { it.total }
    Canvas(modifier) {
        val strokeWidth = size.minDimension * 0.15f
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
private fun CategoryRow(cat: CategoryGroup, maxTotal: Double, onClick: (() -> Unit)? = null) {
    val icon = getLucideIcon(cat.icon)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
    ) {
        Box(
            modifier = Modifier.size(28.dp).clip(CircleShape).background(cat.color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = cat.color)
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(cat.name, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                Text(formatCurrency(BigDecimal.valueOf(cat.total)), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(2.dp))
            Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))) {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surfaceVariant) {}
                Surface(
                    Modifier.fillMaxHeight().fillMaxWidth(fraction = if (maxTotal > 0) (cat.total / maxTotal).toFloat() else 0f),
                    color = cat.color,
                    shape = RoundedCornerShape(3.dp),
                ) {}
            }
        }
    }
}
