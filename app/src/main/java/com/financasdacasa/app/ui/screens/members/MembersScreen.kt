package com.financasdacasa.app.ui.screens.members

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.composables.icons.lucide.*
import com.financasdacasa.app.R
import com.financasdacasa.app.util.resolveServerUrl
import com.financasdacasa.app.data.model.HouseMember

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembersScreen(
    onBack: () -> Unit,
    onNavigateToHouseSelection: () -> Unit,
    viewModel: MembersViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = SnackbarHostState()

    // Navigate to house selection after leaving
    LaunchedEffect(uiState.navigateToHouseSelection) {
        if (uiState.navigateToHouseSelection) {
            onNavigateToHouseSelection()
        }
    }

    // Show snackbar messages
    val removedMsg = stringResource(R.string.members_removed)
    val renamedMsg = stringResource(R.string.members_house_renamed)
    val leftMsg = stringResource(R.string.members_left)
    LaunchedEffect(uiState.snackbar) {
        uiState.snackbar?.let { code ->
            val msg = when (code) {
                "REMOVED" -> removedMsg
                "RENAMED" -> renamedMsg
                "LEFT" -> leftMsg
                else -> code
            }
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    // Show error messages
    val loadErr = stringResource(R.string.error_members_load_failed)
    val removeErr = stringResource(R.string.error_members_remove_failed)
    val leaveErr = stringResource(R.string.error_members_leave_failed)
    val renameErr = stringResource(R.string.error_members_rename_failed)
    val inviteErr = stringResource(R.string.error_members_invite_failed)
    LaunchedEffect(uiState.error) {
        uiState.error?.let { code ->
            val msg = when (code) {
                "LOAD_FAILED" -> loadErr
                "REMOVE_FAILED" -> removeErr
                "LEAVE_FAILED" -> leaveErr
                "RENAME_FAILED" -> renameErr
                "INVITE_FAILED" -> inviteErr
                else -> code
            }
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.members_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Lucide.ArrowLeft, contentDescription = null)
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // House name section
            uiState.house?.let { house ->
                HouseNameSection(
                    houseName = house.name,
                    isOwner = uiState.isOwner,
                    isEditing = uiState.isEditing,
                    editName = uiState.editName,
                    isRenaming = uiState.isRenaming,
                    onStartEditing = viewModel::startEditing,
                    onEditNameChange = viewModel::onEditNameChange,
                    onSave = viewModel::saveRename,
                    onCancel = viewModel::cancelEditing,
                )
            }

            // Members list
            MembersListSection(
                members = uiState.members,
                isOwner = uiState.isOwner,
                onRemove = viewModel::showRemoveDialog,
            )

            // Invite section
            InviteSection(
                inviteLink = uiState.inviteLink,
                isGenerating = uiState.isGeneratingInvite,
                onGenerate = viewModel::generateInvite,
                onShare = { link ->
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.invite_share_title))
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "${context.getString(R.string.invite_share_text)}\n$link",
                        )
                    }
                    context.startActivity(Intent.createChooser(shareIntent, null))
                },
            )

            // Leave house button (non-owners only)
            if (!uiState.isOwner && uiState.currentUserId != null) {
                Button(
                    onClick = viewModel::showLeaveDialog,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.members_leave))
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }

    // Remove confirmation dialog
    uiState.removingMember?.let { member ->
        AlertDialog(
            onDismissRequest = viewModel::dismissRemoveDialog,
            title = {
                Text(stringResource(R.string.members_remove_confirm_title, member.user?.name ?: "?"))
            },
            text = { Text(stringResource(R.string.members_remove_confirm_description)) },
            confirmButton = {
                TextButton(
                    onClick = viewModel::confirmRemove,
                    enabled = !uiState.isRemoving,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text(stringResource(R.string.members_remove))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissRemoveDialog) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    // Leave confirmation dialog
    if (uiState.showLeaveDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissLeaveDialog,
            title = { Text(stringResource(R.string.members_leave_confirm_title)) },
            text = { Text(stringResource(R.string.members_leave_confirm_description)) },
            confirmButton = {
                TextButton(
                    onClick = viewModel::confirmLeave,
                    enabled = !uiState.isLeaving,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text(stringResource(R.string.members_leave))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissLeaveDialog) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun HouseNameSection(
    houseName: String,
    isOwner: Boolean,
    isEditing: Boolean,
    editName: String,
    isRenaming: Boolean,
    onStartEditing: () -> Unit,
    onEditNameChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            stringResource(R.string.members_house_name),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (isEditing) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = editName,
                    onValueChange = onEditNameChange,
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    enabled = !isRenaming,
                )
                IconButton(onClick = onSave, enabled = !isRenaming && editName.isNotBlank()) {
                    Icon(Lucide.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onCancel, enabled = !isRenaming) {
                    Icon(Lucide.X, contentDescription = null)
                }
            }
        } else {
            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 1.dp,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        houseName,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (isOwner) {
                        IconButton(onClick = onStartEditing, modifier = Modifier.size(32.dp)) {
                            Icon(
                                Lucide.Pencil,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MembersListSection(
    members: List<HouseMember>,
    isOwner: Boolean,
    onRemove: (HouseMember) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            stringResource(R.string.members_label),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            members.forEach { member ->
                val isMemberOwner = member.role == "owner"
                val showRemove = isOwner && !isMemberOwner

                Surface(
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 1.dp,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // Avatar
                        val avatarUrl = resolveServerUrl(member.user?.avatarUrl)
                        if (avatarUrl != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(avatarUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = member.user?.name,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            val initial = (member.user?.name?.firstOrNull() ?: '?').uppercaseChar()
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    initial.toString(),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                        }

                        // Name and email
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                member.user?.name ?: "?",
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            member.user?.email?.let { email ->
                                Text(
                                    email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }

                        // Role badge
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = if (isMemberOwner)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant,
                        ) {
                            Text(
                                text = stringResource(
                                    if (isMemberOwner) R.string.members_owner else R.string.members_member,
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = if (isMemberOwner)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        // Remove button
                        if (showRemove) {
                            IconButton(
                                onClick = { onRemove(member) },
                                modifier = Modifier.size(32.dp),
                            ) {
                                Icon(
                                    Lucide.Trash2,
                                    contentDescription = stringResource(R.string.members_remove),
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InviteSection(
    inviteLink: String?,
    isGenerating: Boolean,
    onGenerate: () -> Unit,
    onShare: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            stringResource(R.string.members_invite_section),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            stringResource(R.string.members_invite_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (inviteLink != null) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 1.dp,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        inviteLink,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    IconButton(
                        onClick = { onShare(inviteLink) },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            Lucide.Share2,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        } else {
            OutlinedButton(
                onClick = onGenerate,
                enabled = !isGenerating,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                    )
                    Spacer(Modifier.width(8.dp))
                } else {
                    Icon(Lucide.Share2, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Text(stringResource(R.string.members_generate_invite))
            }
        }
    }
}
