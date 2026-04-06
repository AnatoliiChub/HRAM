package com.achub.hram.screen.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.achub.hram.ActivityNameErrorMapper
import com.achub.hram.ext.stateInExt
import com.achub.hram.model.ActivityInfo
import com.achub.hram.models.GraphLimitsUi
import com.achub.hram.models.HighlightedItemUi
import com.achub.hram.usecase.DeleteActivitiesUseCase
import com.achub.hram.usecase.ExportCsvUseCase
import com.achub.hram.usecase.ObserveActivitiesUseCase
import com.achub.hram.usecase.RenameActivityUseCase
import com.achub.hram.view.cards.ActivityGraphInfo
import com.achub.hram.view.cards.AvgHrBucketByActivity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val MAX_HR_FACTOR = 1.2f

class ActivitiesViewModel(
    val observeActivities: ObserveActivitiesUseCase,
    val deleteActivities: DeleteActivitiesUseCase,
    val renameActivity: RenameActivityUseCase,
    val activityNameErrorMapper: ActivityNameErrorMapper,
    val exportCsvUseCase: ExportCsvUseCase,
    val dispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ActivitiesUiState())

    val uiState: StateFlow<ActivitiesUiState> = _uiState.stateInExt(initialValue = ActivitiesUiState(emptyList()))

    init {
        viewModelScope.launch(dispatcher) {
            observeActivities()
                .map { list -> list.map { it.toGraphInfo() } }
                .collect { activities ->
                    _uiState.update { it.copy(activities = activities) }
                }
        }
    }

    fun onHighlighted(highlightedItem: HighlightedItemUi?) =
        _uiState.update { it.copy(highlightedItem = highlightedItem) }

    fun toggleSelection(id: String) {
        onHighlighted(null)
        toggleActivitySelection(id)
    }

    fun toggleActivitySelection(activityId: String) = _uiState.update { state ->
        val selectedIds = state.selectedActivitiesId
        state.copy(
            selectedActivitiesId = if (selectedIds.contains(activityId)) {
                selectedIds - activityId
            } else {
                selectedIds + activityId
            }
        )
    }

    fun deleteActivities(selectedActivitiesId: Set<String>) {
        viewModelScope.launch(dispatcher) {
            deleteActivities(selectedActivitiesId)
            _uiState.update { state ->
                state.copy(selectedActivitiesId = state.selectedActivitiesId - selectedActivitiesId)
            }
        }
    }

    fun showNameActivityDialog() {
        val state = _uiState.value
        val editedGraph = state.activities.firstOrNull { it.id == state.selectedActivitiesId.firstOrNull() }
        val name = editedGraph?.name ?: ""
        _uiState.update { it.copy(dialog = ActivitiesScreenDialog.ReNameActivity(activityName = name)) }
    }

    fun showActivityDeletionDialog() = _uiState.update {
        it.copy(dialog = ActivitiesScreenDialog.ActivityDeletionDialog)
    }

    fun onActivityNameChanged(name: String) = _uiState.update { state ->
        val currentDialog = state.dialog as? ActivitiesScreenDialog.ReNameActivity
        val error = activityNameErrorMapper(name)
        state.copy(dialog = currentDialog?.copy(activityName = name, error = error))
    }

    fun dismissDialog() = _uiState.update { it.copy(dialog = null) }

    fun exportActivity(id: String) {
        viewModelScope.launch(dispatcher) {
            exportCsvUseCase(id)
        }
    }

    fun onActivityNameConfirmed() {
        val state = _uiState.value
        val currentDialog = state.dialog as? ActivitiesScreenDialog.ReNameActivity ?: return
        val selectedIds = state.selectedActivitiesId
        if (selectedIds.size != 1) return
        val activityId = selectedIds.first()
        viewModelScope.launch(dispatcher) {
            renameActivity(activityId, currentDialog.activityName)
            dismissDialog()
            _uiState.update { it.copy(selectedActivitiesId = emptySet()) }
        }
    }
}

private fun ActivityInfo.toGraphInfo(): ActivityGraphInfo {
    val uiBuckets = buckets.map { AvgHrBucketByActivity(it.bucketNumber, it.avgHr, it.elapsedTime) }
    return ActivityGraphInfo(
        id = id,
        name = name,
        startDate = startDate,
        duration = duration,
        buckets = uiBuckets,
        totalRecords = totalRecords,
        avgHr = avgHr,
        maxHr = maxHr,
        minHr = minHr,
        limits = GraphLimitsUi(
            minX = 0f,
            maxX = uiBuckets.maxOfOrNull { it.elapsedTime }?.toFloat() ?: 1f,
            minY = 0f,
            maxY = (uiBuckets.maxOfOrNull { it.avgHr } ?: 1f) * MAX_HR_FACTOR,
        ),
    )
}
