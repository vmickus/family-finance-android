package com.financasdacasa.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Delete
import com.composables.icons.lucide.Lucide

private data class KeyDef(
    val label: String,
    val value: String,
    val type: KeyType,
)

private enum class KeyType { DIGIT, OPERATOR, BACKSPACE }

@Composable
fun CalculatorKeypad(
    onKey: (String) -> Unit,
    onBackspace: () -> Unit,
    decimalSeparator: String = ",",
    modifier: Modifier = Modifier,
) {
    val rows = listOf(
        listOf(
            KeyDef("7", "7", KeyType.DIGIT),
            KeyDef("8", "8", KeyType.DIGIT),
            KeyDef("9", "9", KeyType.DIGIT),
            KeyDef("\u00F7", "/", KeyType.OPERATOR),
        ),
        listOf(
            KeyDef("4", "4", KeyType.DIGIT),
            KeyDef("5", "5", KeyType.DIGIT),
            KeyDef("6", "6", KeyType.DIGIT),
            KeyDef("\u00D7", "*", KeyType.OPERATOR),
        ),
        listOf(
            KeyDef("1", "1", KeyType.DIGIT),
            KeyDef("2", "2", KeyType.DIGIT),
            KeyDef("3", "3", KeyType.DIGIT),
            KeyDef("-", "-", KeyType.OPERATOR),
        ),
        listOf(
            KeyDef(decimalSeparator, decimalSeparator, KeyType.DIGIT),
            KeyDef("0", "0", KeyType.DIGIT),
            KeyDef("", "", KeyType.BACKSPACE),
            KeyDef("+", "+", KeyType.OPERATOR),
        ),
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (row in rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (key in row) {
                    val shape = RoundedCornerShape(12.dp)
                    val isOperator = key.type == KeyType.OPERATOR
                    val isBackspace = key.type == KeyType.BACKSPACE

                    Surface(
                        color = if (isOperator) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = shape,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(shape)
                            .clickable {
                                when {
                                    isBackspace -> onBackspace()
                                    else -> onKey(key.value)
                                }
                            },
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isBackspace) {
                                Icon(
                                    Lucide.Delete,
                                    contentDescription = "Backspace",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            } else {
                                Text(
                                    text = key.label,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isOperator) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
