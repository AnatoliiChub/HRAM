package com.achub.hram.screen.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.achub.hram.data.HrActivityRepo
import com.achub.hram.data.models.HighlightedItem
import com.achub.hram.ext.stateInExt
import com.achub.hram.utils.ActivityNameValidation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ActivitiesViewModel(
    val hrActivityRepo: HrActivityRepo,
    val activityNameValidation: ActivityNameValidation,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ActivitiesUiState())

    val uiState: StateFlow<ActivitiesUiState> = _uiState.stateInExt(initialValue = ActivitiesUiState(emptyList()))

    init {
        viewModelScope.launch(Dispatchers.Default) {
            hrActivityRepo.getActivitiesGraph()
                .map { list -> list.filter { it.activity.name.isNotEmpty() } }
                .collect { activities ->
                    _uiState.update { it.copy(activities = activities) }
                }
        }
    }

    fun onHighlighted(highlightedItem: HighlightedItem?) =
        _uiState.update { it.copy(highlightedItem = highlightedItem) }

    fun onActivityLongClick(id: String) {
        onHighlighted(null)
        selectActivity(id)
    }

    fun selectActivity(activityId: String) = _uiState.update { state ->
        val selectedIds = state.selectedActivitiesId
        state.copy(
            selectedActivitiesId = if (selectedIds.contains(
                    activityId
                )
            ) {
                selectedIds - activityId
            } else {
                selectedIds + activityId
            }
        )
    }

    fun deleteActivities(selectedActivitiesId: Set<String>) {
        viewModelScope.launch(Dispatchers.Default) {
            hrActivityRepo.deleteActivitiesById(selectedActivitiesId)
            _uiState.update { state ->
                state.copy(selectedActivitiesId = state.selectedActivitiesId - selectedActivitiesId)
            }
        }
    }

    fun showNameActivityDialog() =
        _uiState.update { it.copy(dialog = ActivitiesScreenDialog.ReNameActivity(activityName = "")) }

    fun showActivityDeletionDialog() = _uiState.update {
        it.copy(dialog = ActivitiesScreenDialog.ActivityDeletionDialog)
    }

    fun onActivityNameChanged(name: String) = _uiState.update { state ->
        val currentDialog = state.dialog as? ActivitiesScreenDialog.ReNameActivity
        val error = activityNameValidation(name)
        state.copy(dialog = currentDialog?.copy(activityName = name, error = error))
    }

    fun dismissDialog() = _uiState.update { it.copy(dialog = null) }

    fun onActivityNameConfirmed() {
        val state = _uiState.value
        val currentDialog = state.dialog as? ActivitiesScreenDialog.ReNameActivity ?: return
        val selectedIds = state.selectedActivitiesId
        if (selectedIds.size != 1) return
        val activityId = selectedIds.first()
        viewModelScope.launch(Dispatchers.Default) {
            hrActivityRepo.updateNameById(activityId, currentDialog.activityName)
            dismissDialog()
        }
    }
}
