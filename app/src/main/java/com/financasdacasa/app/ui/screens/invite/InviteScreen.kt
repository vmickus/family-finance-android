package com.financasdacasa.app.ui.screens.invite

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.financasdacasa.app.R

@Composable
fun InviteScreen(
    onNavigateToLogin: (inviteToken: String) -> Unit,
    onNavigateToRegister: (inviteToken: String) -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: InviteViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    // Navigate after successful accept
    LaunchedEffect(uiState.accepted) {
        if (uiState.accepted) {
            onNavigateToHome()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        when {
            uiState.isLoading -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.invite_loading),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            uiState.error == "INVALID" || uiState.error == "LOAD_FAILED" -> {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            stringResource(R.string.invite_invalid),
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            stringResource(R.string.invite_invalid_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                        Button(
                            onClick = { onNavigateToLogin("") },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(R.string.login))
                        }
                    }
                }
            }

            uiState.details != null -> {
                val details = uiState.details!!
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(
                            stringResource(R.string.invite_title),
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                        )

                        if (details.isValid) {
                            Text(
                                text = if (details.ownerName != null) {
                                    stringResource(
                                        R.string.invite_valid_description_with_owner,
                                        details.houseName ?: "",
                                        details.ownerName,
                                    )
                                } else {
                                    stringResource(
                                        R.string.invite_valid_description,
                                        details.houseName ?: "",
                                    )
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )

                            if (uiState.isAuthenticated) {
                                // Authenticated: show Join button
                                Button(
                                    onClick = viewModel::acceptInvite,
                                    enabled = !uiState.isAccepting,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    if (uiState.isAccepting) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(stringResource(R.string.invite_joining))
                                    } else {
                                        Text(stringResource(R.string.invite_join))
                                    }
                                }

                                if (uiState.error == "ACCEPT_FAILED") {
                                    Text(
                                        stringResource(R.string.error_invite_accept_failed),
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            } else {
                                // Not authenticated: show Login / Register buttons
                                Button(
                                    onClick = { onNavigateToLogin(uiState.token) },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text(stringResource(R.string.invite_already_have_account))
                                }
                                OutlinedButton(
                                    onClick = { onNavigateToRegister(uiState.token) },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text(stringResource(R.string.invite_create_and_join))
                                }
                            }
                        } else {
                            // Expired or invalid
                            Text(
                                stringResource(R.string.invite_expired_description),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                            Button(
                                onClick = { onNavigateToLogin("") },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(stringResource(R.string.login))
                            }
                        }
                    }
                }
            }
        }
    }
}
