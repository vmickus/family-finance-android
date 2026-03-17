package com.financasdacasa.app.ui.screens.privacy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Trash2
import com.composables.icons.lucide.TriangleAlert
import com.financasdacasa.app.R
import com.financasdacasa.app.data.model.OwnedHouseInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeactivateAccountDialog(
    open: Boolean,
    onDismiss: () -> Unit,
    viewModel: DeactivateViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val isGoogleUser by viewModel.isGoogleUser.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val errorMsg = stringResource(R.string.deactivation_error)
    val transferSuccessMsg = stringResource(R.string.deactivation_transfer_success)
    val deleteSuccessMsg = stringResource(R.string.deactivation_delete_success)

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(errorMsg)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.snackbar) {
        uiState.snackbar?.let { code ->
            val msg = when (code) {
                "TRANSFER_SUCCESS" -> transferSuccessMsg
                "DELETE_SUCCESS" -> deleteSuccessMsg
                else -> return@let
            }
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    // Reset state when dialog opens
    LaunchedEffect(open) {
        if (open) viewModel.reset()
    }

    if (!open) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Lucide.TriangleAlert,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    stringResource(R.string.deactivation_title),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                when (uiState.step) {
                    DeactivateStep.WARNING -> WarningStep()
                    DeactivateStep.HOUSES -> HousesStep(
                        houses = uiState.ownedHouses,
                        ownerSelections = uiState.ownerSelections,
                        isLoading = uiState.isLoading,
                        onOwnerSelected = viewModel::onOwnerSelected,
                        onTransfer = viewModel::transferOwnership,
                        onDelete = viewModel::deleteHouse,
                    )
                    DeactivateStep.CONFIRM -> ConfirmStep(
                        isGoogleUser = isGoogleUser,
                        password = uiState.password,
                        onPasswordChange = viewModel::onPasswordChange,
                    )
                }
            }
        },
        confirmButton = {
            when (uiState.step) {
                DeactivateStep.WARNING -> {
                    Button(
                        onClick = viewModel::proceedFromWarning,
                        enabled = !uiState.isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                        ),
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onError,
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(stringResource(R.string.deactivation_continue))
                    }
                }
                DeactivateStep.HOUSES -> {
                    // No confirm button on houses step — actions are per-house
                }
                DeactivateStep.CONFIRM -> {
                    Button(
                        onClick = viewModel::deactivateAccount,
                        enabled = !uiState.isSubmitting && (isGoogleUser || uiState.password.isNotBlank()),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                        ),
                    ) {
                        if (uiState.isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onError,
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(stringResource(R.string.deactivation_button))
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
private fun WarningStep() {
    Text(
        stringResource(R.string.deactivation_warning_description),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "• ${stringResource(R.string.deactivation_warning1)}",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                "• ${stringResource(R.string.deactivation_warning2)}",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                "• ${stringResource(R.string.deactivation_warning3)}",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun HousesStep(
    houses: List<OwnedHouseInfo>,
    ownerSelections: Map<String, String>,
    isLoading: Boolean,
    onOwnerSelected: (String, String) -> Unit,
    onTransfer: (String) -> Unit,
    onDelete: (String) -> Unit,
) {
    Text(
        stringResource(R.string.deactivation_houses_description),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    houses.forEach { house ->
        HouseCard(
            house = house,
            selectedOwnerId = ownerSelections[house.id],
            isLoading = isLoading,
            onOwnerSelected = { memberId -> onOwnerSelected(house.id, memberId) },
            onTransfer = { onTransfer(house.id) },
            onDelete = { onDelete(house.id) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HouseCard(
    house: OwnedHouseInfo,
    selectedOwnerId: String?,
    isLoading: Boolean,
    onOwnerSelected: (String) -> Unit,
    onTransfer: () -> Unit,
    onDelete: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    OutlinedCard {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                house.name,
                style = MaterialTheme.typography.titleSmall,
            )

            if (house.members.isNotEmpty()) {
                Text(
                    stringResource(R.string.deactivation_transfer_to),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                ) {
                    val selectedMember = house.members.find { it.id == selectedOwnerId }
                    OutlinedTextField(
                        value = selectedMember?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text(stringResource(R.string.deactivation_select_member)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        singleLine = true,
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        house.members.forEach { member ->
                            DropdownMenuItem(
                                text = { Text(member.name) },
                                onClick = {
                                    onOwnerSelected(member.id)
                                    expanded = false
                                },
                            )
                        }
                    }
                }
                Button(
                    onClick = onTransfer,
                    enabled = selectedOwnerId != null && !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Spacer(Modifier.width(8.dp))
                    } else {
                        Icon(
                            Lucide.ArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(stringResource(R.string.deactivation_transfer))
                }
            } else {
                Text(
                    stringResource(R.string.deactivation_no_members),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(
                    onClick = onDelete,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onError,
                        )
                        Spacer(Modifier.width(8.dp))
                    } else {
                        Icon(
                            Lucide.Trash2,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(stringResource(R.string.deactivation_delete_house))
                }
            }
        }
    }
}

@Composable
private fun ConfirmStep(
    isGoogleUser: Boolean,
    password: String,
    onPasswordChange: (String) -> Unit,
) {
    Text(
        stringResource(R.string.deactivation_confirm_description),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    if (!isGoogleUser) {
        Text(
            stringResource(R.string.deactivation_enter_password),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            visualTransformation = PasswordVisualTransformation(),
            placeholder = { Text("••••••••") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
