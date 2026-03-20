package com.financasdacasa.app.ui.screens.more

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.composables.icons.lucide.*
import com.financasdacasa.app.R
import com.financasdacasa.app.data.local.ThemeMode
import com.financasdacasa.app.util.resolveServerUrl
import com.financasdacasa.app.ui.components.ImageCropDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onBack: () -> Unit,
    onMembers: () -> Unit,
    onCategories: () -> Unit,
    onRecurring: () -> Unit,
    onSubscription: () -> Unit,
    onPrivacy: () -> Unit,
    viewModel: MoreViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val isDarkTheme = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showSourcePicker by remember { mutableStateOf(false) }
    var cropUri by remember { mutableStateOf<Uri?>(null) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    // Gallery picker
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri -> if (uri != null) cropUri = uri }

    // Camera
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { success -> if (success && cameraUri != null) cropUri = cameraUri }

    // Camera permission
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            val uri = viewModel.getCameraFileUri()
            cameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    // Snackbar messages
    val avatarUpdatedMsg = stringResource(R.string.avatar_updated)
    val avatarDeletedMsg = stringResource(R.string.avatar_deleted)
    LaunchedEffect(uiState.snackbar) {
        uiState.snackbar?.let { code ->
            val msg = when (code) {
                "AVATAR_UPDATED" -> avatarUpdatedMsg
                "AVATAR_DELETED" -> avatarDeletedMsg
                else -> code
            }
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    // Error messages
    val uploadErr = stringResource(R.string.error_avatar_upload_failed)
    val deleteErr = stringResource(R.string.error_avatar_delete_failed)
    LaunchedEffect(uiState.error) {
        uiState.error?.let { code ->
            val msg = when (code) {
                "UPLOAD_FAILED" -> uploadErr
                "DELETE_FAILED" -> deleteErr
                else -> code
            }
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError()
        }
    }

    // Crop dialog
    cropUri?.let { uri ->
        ImageCropDialog(
            imageUri = uri,
            onConfirm = { bitmap ->
                cropUri = null
                viewModel.uploadAvatar(bitmap)
            },
            onDismiss = { cropUri = null },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.more_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Lucide.ArrowLeft, contentDescription = null)
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // Profile header
            user?.let { u ->
                ProfileHeader(
                    name = u.name,
                    email = u.email,
                    avatarUrl = resolveServerUrl(u.avatarUrl),
                    isUploading = uiState.isUploading || uiState.isDeleting,
                    onAvatarClick = { showSourcePicker = true },
                    onDeleteAvatar = if (u.avatarUrl != null) {
                        { viewModel.showDeleteConfirm() }
                    } else null,
                )
                Spacer(Modifier.height(12.dp))
            }

            // Menu items
            MenuItem(icon = Lucide.Users, label = stringResource(R.string.nav_members), onClick = onMembers)
            MenuItem(icon = Lucide.Grid2x2, label = stringResource(R.string.categories), onClick = onCategories)
            MenuItem(icon = Lucide.Repeat, label = stringResource(R.string.recurring_title), onClick = onRecurring)
            MenuItem(icon = Lucide.CreditCard, label = stringResource(R.string.subscription_title), onClick = onSubscription)

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            MenuItem(icon = Lucide.Shield, label = stringResource(R.string.privacy_menu_button), onClick = onPrivacy)
            MenuItem(
                icon = if (isDarkTheme) Lucide.Sun else Lucide.Moon,
                label = if (isDarkTheme) stringResource(R.string.theme_light_mode) else stringResource(R.string.theme_dark_mode),
                onClick = {
                    viewModel.setThemeMode(if (isDarkTheme) ThemeMode.LIGHT else ThemeMode.DARK)
                },
            )
        }
    }

    // Source picker bottom sheet
    if (showSourcePicker) {
        ModalBottomSheet(
            onDismissRequest = { showSourcePicker = false },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
            ) {
                Text(
                    stringResource(R.string.avatar_choose_source),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                )
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showSourcePicker = false
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Icon(Lucide.Camera, contentDescription = null)
                        Text(stringResource(R.string.avatar_camera), style = MaterialTheme.typography.bodyLarge)
                    }
                }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showSourcePicker = false
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                            )
                        },
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Icon(Lucide.Image, contentDescription = null)
                        Text(stringResource(R.string.avatar_gallery), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (uiState.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteConfirm,
            title = { Text(stringResource(R.string.avatar_delete_title)) },
            text = { Text(stringResource(R.string.avatar_delete_description)) },
            confirmButton = {
                TextButton(
                    onClick = viewModel::deleteAvatar,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDeleteConfirm) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun ProfileHeader(
    name: String,
    email: String,
    avatarUrl: String?,
    isUploading: Boolean,
    onAvatarClick: () -> Unit,
    onDeleteAvatar: (() -> Unit)?,
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Avatar
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(56.dp),
                        strokeWidth = 2.dp,
                    )
                } else if (avatarUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(avatarUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = name,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onAvatarClick),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    val initial = (name.firstOrNull() ?: '?').uppercaseChar()
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable(onClick = onAvatarClick),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            initial.toString(),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }

                // Camera badge
                if (!isUploading) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable(onClick = onAvatarClick),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Lucide.Camera,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            }

            // Name + email
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Delete avatar button
            if (onDeleteAvatar != null && !isUploading) {
                IconButton(
                    onClick = onDeleteAvatar,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        Lucide.Trash2,
                        contentDescription = stringResource(R.string.avatar_delete_title),
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(icon, contentDescription = null)
            Text(label, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
