package com.financasdacasa.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.financasdacasa.app.util.hasOperator
import com.financasdacasa.app.util.normalizeExpressionInput
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CalculatorAmountField(
    value: String,
    onValueChange: (String) -> Unit,
    evaluatedAmount: Double?,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    currencySymbol: String = "R$",
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var isFocused by remember { mutableStateOf(false) }

    var textFieldValue by remember(value) {
        mutableStateOf(
            TextFieldValue(
                text = value,
                selection = TextRange(value.length),
            ),
        )
    }

    val currencyFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    }

    val showPreview = hasOperator(value) && evaluatedAmount != null

    Column(modifier = modifier) {
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                val normalized = normalizeExpressionInput(newValue.text)
                textFieldValue = newValue.copy(text = normalized)
                onValueChange(normalized)
            },
            label = label,
            isError = isError,
            singleLine = true,
            prefix = { Text(currencySymbol) },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                    if (focusState.isFocused) {
                        keyboardController?.hide()
                    }
                },
        )

        if (showPreview && evaluatedAmount != null) {
            Text(
                text = "= ${currencyFormat.format(evaluatedAmount)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp),
            )
        }

        if (isFocused) {
            Spacer(Modifier.height(12.dp))
            CalculatorKeypad(
                onKey = { key ->
                    val cursorPos = textFieldValue.selection.start
                    val newText = textFieldValue.text.substring(0, cursorPos) +
                        key +
                        textFieldValue.text.substring(cursorPos)
                    val normalized = normalizeExpressionInput(newText)
                    val newCursor = (cursorPos + key.length).coerceAtMost(normalized.length)
                    textFieldValue = TextFieldValue(
                        text = normalized,
                        selection = TextRange(newCursor),
                    )
                    onValueChange(normalized)
                },
                onBackspace = {
                    val cursorPos = textFieldValue.selection.start
                    if (cursorPos > 0) {
                        val newText = textFieldValue.text.removeRange(cursorPos - 1, cursorPos)
                        val normalized = normalizeExpressionInput(newText)
                        textFieldValue = TextFieldValue(
                            text = normalized,
                            selection = TextRange((cursorPos - 1).coerceAtLeast(0)),
                        )
                        onValueChange(normalized)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
