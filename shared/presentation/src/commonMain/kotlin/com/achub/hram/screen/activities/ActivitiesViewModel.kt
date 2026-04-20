package com.achub.hram.screen.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.achub.hram.ActivityNameErrorMapper
import com.achub.hram.ext.launchIn
import com.achub.hram.ext.stateInExt
import com.achub.hram.models.HighlightedItemUi
import com.achub.hram.usecase.DeleteActivitiesUseCase
import com.achub.hram.usecase.ExportCsvUseCase
import com.achub.hram.usecase.ObserveActivitiesUseCase
import com.achub.hram.usecase.RenameActivityUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val PAGE_SIZE = 7

@OptIn(ExperimentalCoroutinesApi::class)
class ActivitiesViewModel(
    val observeActivities: ObserveActivitiesUseCase,
    val deleteActivitiesUseCase: DeleteActivitiesUseCase,
    val renameActivity: RenameActivityUseCase,
    val activityNameErrorMapper: ActivityNameErrorMapper,
    val exportCsvUseCase: ExportCsvUseCase,
    val dispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ActivitiesUiState())

    val uiState: StateFlow<ActivitiesUiState> = _uiState.stateInExt(initialValue = ActivitiesUiState(emptyList()))

    private val limit = MutableStateFlow(PAGE_SIZE)

    init {
        limit.onEach { _uiState.update { it.copy(isLoading = true) } }
            .flatMapLatest { observeActivities(it) }
            .map { list -> list.map { it.toGraphInfo() } }
            .onEach { activities ->
                _uiState.update { it.copy(activities = activities, isLoading = false) }
            }
            .flowOn(dispatcher)
            .launchIn(viewModelScope)
    }

    fun loadMore() {
        limit.update { it + PAGE_SIZE }
    }

    fun onHighlighted(highlightedItem: HighlightedItemUi?) =
        _uiState.update { it.copy(highlightedItem = highlightedItem) }

    fun toggleSelection(id: String) {
        onHighlighted(null)
        toggleActivitySelection(id)
    }

    fun toggleActivitySelection(activityId: String) = _uiState.update { state ->
        val selectedIds = state.selectedActivitiesId
        val idList = if (selectedIds.contains(activityId)) selectedIds - activityId else selectedIds + activityId
        state.copy(selectedActivitiesId = idList)
    }

    fun deleteActivities(selectedActivitiesId: Set<String>) {
        viewModelScope.launch(dispatcher) {
            deleteActivitiesUseCase(selectedActivitiesId)
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

    fun exportActivity(id: String) = viewModelScope.launch(dispatcher) { exportCsvUseCase(id) }

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
