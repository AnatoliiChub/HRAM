package com.achub.hram.screen.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.achub.hram.data.HrActivityRepo
import com.achub.hram.data.models.HighlightedItem
import com.achub.hram.stateInExt
import com.achub.hram.utils.ActivityNameValidation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ActivitiesViewModel(
    val hrActivityRepo: HrActivityRepo,
    val activityNameValidation: ActivityNameValidation,
) : ViewModel() {

    private val highlightedItem = MutableStateFlow<HighlightedItem?>(null)
    private val selectedActivitiesId = MutableStateFlow<Set<String>>(emptySet())
    private val dialog: MutableStateFlow<ActivitiesScreenDialog?> = MutableStateFlow(null)
    private val _uiState = combine(
        flow = hrActivityRepo.getActivitiesGraph()
            .map { list -> list.filter { it.activity.name.isNotEmpty() } },
        flow2 = highlightedItem,
        flow3 = selectedActivitiesId,
        flow4 = dialog,
    ) { activities, highlightedItem, selectedIds, dialog ->
        ActivitiesUiState(
            list = activities,
            highlightedItem = highlightedItem,
            selectedActivitiesId = selectedIds,
            dialog = dialog
        )
    }.flowOn(Dispatchers.Default)
    val uiState: StateFlow<ActivitiesUiState> = _uiState.stateInExt(initialValue = ActivitiesUiState(emptyList()))

    fun onHighlighted(highlightedItem: HighlightedItem?) = this.highlightedItem.update { highlightedItem }

    fun selectActivity(activityId: String) = selectedActivitiesId.update { selectedIds ->
        if (selectedIds.contains(activityId)) selectedIds - activityId else selectedIds + activityId
    }

    fun deleteActivities(selectedActivitiesId: Set<String>) {
        viewModelScope.launch(Dispatchers.Default) {
            hrActivityRepo.deleteActivitiesById(selectedActivitiesId)
            this@ActivitiesViewModel.selectedActivitiesId.update { currentSelectedIds ->
                currentSelectedIds - selectedActivitiesId
            }
        }
    }

    fun showNameActivityDialog() = dialog.update { ActivitiesScreenDialog.ReNameActivity(activityName = "") }

    fun showActivityDeletionDialog() = dialog.update { ActivitiesScreenDialog.ActivityDeletionDialog }
    fun onActivityNameChanged(name: String) = dialog.update { dialog ->
        val currentDialog = dialog as? ActivitiesScreenDialog.ReNameActivity
        val error = activityNameValidation(name)
        currentDialog?.copy(activityName = name, error = error)
    }

    fun dismissDialog() = dialog.update { null }

    fun onActivityNameConfirmed() {
        val currentDialog = dialog.value as? ActivitiesScreenDialog.ReNameActivity ?: return
        val selectedIds = selectedActivitiesId.value
        if (selectedIds.size != 1) return
        val activityId = selectedIds.first()
        viewModelScope.launch(Dispatchers.Default) {
            hrActivityRepo.updateNameById(activityId, currentDialog.activityName)
            dismissDialog()
        }
    }

}
