package com.financasdacasa.app.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.financasdacasa.app.R
import com.financasdacasa.app.data.model.Transaction

@Composable
fun DeleteTransactionDialog(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onDeleteSingle: () -> Unit,
    onCancelRecurring: () -> Unit,
) {
    val isRecurring = transaction.recurringTransactionId != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(
                    if (isRecurring) R.string.delete_recurring_title
                    else R.string.delete_transaction_title,
                ),
            )
        },
        text = {
            Text(
                stringResource(
                    if (isRecurring) R.string.delete_recurring_description
                    else R.string.delete_transaction_description,
                ),
            )
        },
        confirmButton = {
            TextButton(onClick = onDeleteSingle) {
                Text(
                    stringResource(
                        if (isRecurring) R.string.delete_this_only else R.string.delete,
                    ),
                )
            }
        },
        dismissButton = {
            if (isRecurring) {
                TextButton(onClick = onCancelRecurring) {
                    Text(stringResource(R.string.cancel_from_date))
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }
        },
    )
}
