package com.financasdacasa.app.ui.screens.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financasdacasa.app.data.local.SessionManager
import com.financasdacasa.app.data.model.Category
import com.financasdacasa.app.data.model.CreateCategoryRequest
import com.financasdacasa.app.data.model.ReorderCategoriesRequest
import com.financasdacasa.app.data.model.UpdateCategoryRequest
import com.financasdacasa.app.data.repository.CategoryRepository
import com.financasdacasa.app.util.DEFAULT_CATEGORY_COLOR
import com.financasdacasa.app.util.DEFAULT_CATEGORY_ICON
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoriesUiState(
    val selectedType: String = "expense",
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    // Form state
    val showForm: Boolean = false,
    val editingCategory: Category? = null,
    val formName: String = "",
    val formIcon: String = DEFAULT_CATEGORY_ICON,
    val formColor: String = DEFAULT_CATEGORY_COLOR,
    val formError: String? = null,
    val isSaving: Boolean = false,
    // Delete state
    val deletingCategory: Category? = null,
)

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    private val houseId: String? get() = sessionManager.getSelectedHouseId()

    init {
        loadCategories()
    }

    fun loadCategories() {
        val id = houseId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val categories = categoryRepository.list(id, _uiState.value.selectedType)
                _uiState.value = _uiState.value.copy(
                    categories = categories.sortedBy { it.position },
                    isLoading = false,
                )
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "LOAD_FAILED")
            }
        }
    }

    fun onTypeChange(type: String) {
        _uiState.value = _uiState.value.copy(selectedType = type)
        loadCategories()
    }

    // --- Form ---

    fun showCreateForm() {
        _uiState.value = _uiState.value.copy(
            showForm = true,
            editingCategory = null,
            formName = "",
            formIcon = DEFAULT_CATEGORY_ICON,
            formColor = DEFAULT_CATEGORY_COLOR,
            formError = null,
        )
    }

    fun showEditForm(category: Category) {
        _uiState.value = _uiState.value.copy(
            showForm = true,
            editingCategory = category,
            formName = category.name,
            formIcon = category.icon,
            formColor = category.color,
            formError = null,
        )
    }

    fun dismissForm() {
        _uiState.value = _uiState.value.copy(showForm = false, editingCategory = null)
    }

    fun onFormNameChange(name: String) {
        _uiState.value = _uiState.value.copy(formName = name, formError = null)
    }

    fun onFormIconChange(icon: String) {
        _uiState.value = _uiState.value.copy(formIcon = icon)
    }

    fun onFormColorChange(color: String) {
        _uiState.value = _uiState.value.copy(formColor = color)
    }

    fun saveCategory() {
        val state = _uiState.value
        val name = state.formName.trim()
        if (name.isEmpty()) {
            _uiState.value = state.copy(formError = "NAME_REQUIRED")
            return
        }
        if (name.length > 50) {
            _uiState.value = state.copy(formError = "NAME_MAX")
            return
        }

        val id = houseId ?: return
        _uiState.value = state.copy(isSaving = true, formError = null)

        viewModelScope.launch {
            try {
                val editing = state.editingCategory
                if (editing != null) {
                    categoryRepository.update(
                        editing.id,
                        UpdateCategoryRequest(name = name, color = state.formColor, icon = state.formIcon),
                    )
                } else {
                    categoryRepository.create(
                        CreateCategoryRequest(
                            houseId = id,
                            name = name,
                            color = state.formColor,
                            icon = state.formIcon,
                            type = state.selectedType,
                        ),
                    )
                }
                _uiState.value = _uiState.value.copy(showForm = false, editingCategory = null, isSaving = false)
                loadCategories()
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, formError = "SAVE_FAILED")
            }
        }
    }

    // --- Delete ---

    fun showDeleteConfirm(category: Category) {
        _uiState.value = _uiState.value.copy(deletingCategory = category)
    }

    fun dismissDelete() {
        _uiState.value = _uiState.value.copy(deletingCategory = null)
    }

    fun deleteCategory() {
        val category = _uiState.value.deletingCategory ?: return
        viewModelScope.launch {
            try {
                categoryRepository.delete(category.id)
                _uiState.value = _uiState.value.copy(deletingCategory = null)
                loadCategories()
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(deletingCategory = null, error = "DELETE_FAILED")
            }
        }
    }

    // --- Reorder ---

    fun onReorder(fromIndex: Int, toIndex: Int) {
        val current = _uiState.value.categories.toMutableList()
        val item = current.removeAt(fromIndex)
        current.add(toIndex, item)
        _uiState.value = _uiState.value.copy(categories = current)
    }

    fun commitReorder() {
        val id = houseId ?: return
        val state = _uiState.value
        viewModelScope.launch {
            try {
                categoryRepository.reorder(
                    ReorderCategoriesRequest(
                        houseId = id,
                        type = state.selectedType,
                        categoryIds = state.categories.map { it.id },
                    ),
                )
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(error = "REORDER_FAILED")
                loadCategories()
            }
        }
    }
}
