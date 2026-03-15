package com.financasdacasa.app.ui.screens.more

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financasdacasa.app.data.local.AuthState
import com.financasdacasa.app.data.local.SessionManager
import com.financasdacasa.app.data.model.User
import com.financasdacasa.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

data class MoreUiState(
    val isUploading: Boolean = false,
    val isDeleting: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val error: String? = null,
    val snackbar: String? = null,
)

@HiltViewModel
class MoreViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MoreUiState())
    val uiState: StateFlow<MoreUiState> = _uiState.asStateFlow()

    val currentUser: StateFlow<User?> = sessionManager.authState
        .map { (it as? AuthState.Authenticated)?.user }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun getCameraFileUri(): Uri {
        val dir = File(appContext.cacheDir, "camera_photos").apply { mkdirs() }
        val file = File(dir, "photo_${System.currentTimeMillis()}.jpg")
        return androidx.core.content.FileProvider.getUriForFile(
            appContext,
            "${appContext.packageName}.fileprovider",
            file,
        )
    }

    fun uploadAvatar(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploading = true, error = null)
            try {
                val bytes = compressToJpeg(bitmap, quality = 85, maxBytes = 2 * 1024 * 1024)
                val body = bytes.toRequestBody("image/jpeg".toMediaType())
                val part = MultipartBody.Part.createFormData("avatar", "avatar.jpg", body)
                authRepository.uploadAvatar(part)
                // Refresh user
                val user = authRepository.getMe()
                sessionManager.updateUser(user)
                _uiState.value = _uiState.value.copy(isUploading = false, snackbar = "AVATAR_UPDATED")
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isUploading = false, error = "UPLOAD_FAILED")
            }
        }
    }

    fun showDeleteConfirm() {
        _uiState.value = _uiState.value.copy(showDeleteConfirm = true)
    }

    fun dismissDeleteConfirm() {
        _uiState.value = _uiState.value.copy(showDeleteConfirm = false)
    }

    fun deleteAvatar() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, showDeleteConfirm = false, error = null)
            try {
                authRepository.deleteAvatar()
                val user = authRepository.getMe()
                sessionManager.updateUser(user)
                _uiState.value = _uiState.value.copy(isDeleting = false, snackbar = "AVATAR_DELETED")
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isDeleting = false, error = "DELETE_FAILED")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSnackbar() {
        _uiState.value = _uiState.value.copy(snackbar = null)
    }

    private fun compressToJpeg(bitmap: Bitmap, quality: Int, maxBytes: Int): ByteArray {
        var q = quality
        while (q > 10) {
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, q, baos)
            val bytes = baos.toByteArray()
            if (bytes.size <= maxBytes) return bytes
            q -= 10
        }
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 10, baos)
        return baos.toByteArray()
    }
}
