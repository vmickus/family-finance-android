package com.financasdacasa.app.ui.screens.members

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financasdacasa.app.data.local.SessionManager
import com.financasdacasa.app.data.local.AuthState
import com.financasdacasa.app.data.model.House
import com.financasdacasa.app.data.model.HouseMember
import com.financasdacasa.app.data.repository.HouseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MembersUiState(
    val members: List<HouseMember> = emptyList(),
    val house: House? = null,
    val isLoading: Boolean = true,
    val isOwner: Boolean = false,
    val currentUserId: String? = null,
    // Rename
    val isEditing: Boolean = false,
    val editName: String = "",
    val isRenaming: Boolean = false,
    // Invite
    val inviteLink: String? = null,
    val isGeneratingInvite: Boolean = false,
    // Remove
    val removingMember: HouseMember? = null,
    val isRemoving: Boolean = false,
    // Leave
    val showLeaveDialog: Boolean = false,
    val isLeaving: Boolean = false,
    // Feedback
    val snackbar: String? = null,
    val error: String? = null,
    // Navigation event
    val navigateToHouseSelection: Boolean = false,
)

@HiltViewModel
class MembersViewModel @Inject constructor(
    private val houseRepository: HouseRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MembersUiState())
    val uiState: StateFlow<MembersUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        val houseId = sessionManager.getSelectedHouseId() ?: return
        val userId = (sessionManager.authState.value as? AuthState.Authenticated)?.user?.id

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, currentUserId = userId)
            try {
                val members = houseRepository.getMembers(houseId)
                val houses = houseRepository.list()
                val house = houses.find { it.id == houseId }
                val isOwner = house?.ownerId == userId

                _uiState.value = _uiState.value.copy(
                    members = members,
                    house = house,
                    isOwner = isOwner,
                    isLoading = false,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "LOAD_FAILED")
            }
        }
    }

    // ---- Rename ----

    fun startEditing() {
        val name = _uiState.value.house?.name ?: return
        _uiState.value = _uiState.value.copy(isEditing = true, editName = name)
    }

    fun onEditNameChange(value: String) {
        _uiState.value = _uiState.value.copy(editName = value)
    }

    fun cancelEditing() {
        _uiState.value = _uiState.value.copy(isEditing = false, editName = "")
    }

    fun saveRename() {
        val houseId = sessionManager.getSelectedHouseId() ?: return
        val name = _uiState.value.editName.trim()
        if (name.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRenaming = true)
            try {
                val updated = houseRepository.rename(houseId, name)
                _uiState.value = _uiState.value.copy(
                    house = updated,
                    isEditing = false,
                    isRenaming = false,
                    snackbar = "RENAMED",
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isRenaming = false, error = "RENAME_FAILED")
            }
        }
    }

    // ---- Invite ----

    fun generateInvite() {
        val houseId = sessionManager.getSelectedHouseId() ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGeneratingInvite = true)
            try {
                val invite = houseRepository.invite(houseId)
                val path = invite.inviteUrl ?: "/invite/${invite.token}"
                val link = "https://app.financasdacasa.com.br$path"
                _uiState.value = _uiState.value.copy(
                    inviteLink = link,
                    isGeneratingInvite = false,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isGeneratingInvite = false, error = "INVITE_FAILED")
            }
        }
    }

    // ---- Remove ----

    fun showRemoveDialog(member: HouseMember) {
        _uiState.value = _uiState.value.copy(removingMember = member)
    }

    fun dismissRemoveDialog() {
        _uiState.value = _uiState.value.copy(removingMember = null)
    }

    fun confirmRemove() {
        val houseId = sessionManager.getSelectedHouseId() ?: return
        val member = _uiState.value.removingMember ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRemoving = true)
            try {
                houseRepository.removeMember(houseId, member.userId)
                _uiState.value = _uiState.value.copy(
                    members = _uiState.value.members.filter { it.id != member.id },
                    removingMember = null,
                    isRemoving = false,
                    snackbar = "REMOVED",
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    removingMember = null,
                    isRemoving = false,
                    error = "REMOVE_FAILED",
                )
            }
        }
    }

    // ---- Leave ----

    fun showLeaveDialog() {
        _uiState.value = _uiState.value.copy(showLeaveDialog = true)
    }

    fun dismissLeaveDialog() {
        _uiState.value = _uiState.value.copy(showLeaveDialog = false)
    }

    fun confirmLeave() {
        val houseId = sessionManager.getSelectedHouseId() ?: return
        val userId = _uiState.value.currentUserId ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLeaving = true)
            try {
                houseRepository.removeMember(houseId, userId)
                sessionManager.selectHouse("")
                _uiState.value = _uiState.value.copy(
                    showLeaveDialog = false,
                    isLeaving = false,
                    snackbar = "LEFT",
                    navigateToHouseSelection = true,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    showLeaveDialog = false,
                    isLeaving = false,
                    error = "LEAVE_FAILED",
                )
            }
        }
    }

    // ---- Feedback ----

    fun clearSnackbar() {
        _uiState.value = _uiState.value.copy(snackbar = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
