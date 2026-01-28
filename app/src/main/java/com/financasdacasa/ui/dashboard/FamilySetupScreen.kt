package com.financasdacasa.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilySetupScreen(
    isLoading: Boolean,
    error: String?,
    onCreateFamily: (String) -> Unit,
    onJoinFamily: (String, String) -> Unit,
    onLogout: () -> Unit
) {
    var mode by remember { mutableStateOf<String?>(null) }
    var familyName by remember { mutableStateOf("") }
    var familyId by remember { mutableStateOf("") }
    var token by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurar Família") },
                navigationIcon = {
                    if (mode != null) {
                        IconButton(onClick = { mode = null }) {
                            Icon(Icons.Default.ArrowBack, "Voltar")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, "Sair")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            error?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            when (mode) {
                null -> {
                    Text(
                        text = "Para começar, você precisa criar ou entrar em uma família.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { mode = "create" }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Group,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    "Criar Nova Família",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "Crie uma família e convide pessoas",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { mode = "join" }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    "Entrar em uma Família",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "Use um convite para entrar",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                "create" -> {
                    Text(
                        text = "Criar Nova Família",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    OutlinedTextField(
                        value = familyName,
                        onValueChange = { familyName = it },
                        label = { Text("Nome da Família") },
                        placeholder = { Text("Ex: Família Silva") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { onCreateFamily(familyName) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading && familyName.isNotBlank()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Criar Família")
                        }
                    }
                }

                "join" -> {
                    Text(
                        text = "Entrar em uma Família",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    OutlinedTextField(
                        value = familyId,
                        onValueChange = { familyId = it },
                        label = { Text("ID da Família") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = token,
                        onValueChange = { token = it },
                        label = { Text("Token de Convite") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { onJoinFamily(familyId, token) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading && familyId.isNotBlank() && token.isNotBlank()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Entrar na Família")
                        }
                    }
                }
            }
        }
    }
}
