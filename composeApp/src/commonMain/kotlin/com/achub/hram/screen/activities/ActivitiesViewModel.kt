package com.achub.hram.screen.activities

import androidx.lifecycle.ViewModel
import com.achub.hram.data.HrActivityRepo
import com.achub.hram.data.models.HighlightedItem
import com.achub.hram.stateInExt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class ActivitiesViewModel(val hrActivityRepo: HrActivityRepo) : ViewModel() {

    val highlightedItem = MutableStateFlow<HighlightedItem?>(null)
    private val _uiState = combine(
        flow = hrActivityRepo.getActivitiesGraph()
            .map { list -> list.filter { it.activity.name.isNotEmpty() } },
        flow2 = highlightedItem
    ) { activities, highlightedItem -> ActivitiesUiState(activities, highlightedItem) }
        .flowOn(Dispatchers.Default)
    val uiState: StateFlow<ActivitiesUiState> = _uiState.stateInExt(initialValue = ActivitiesUiState(emptyList()))

    fun onHighlighted(highlightedItem: HighlightedItem?) = this.highlightedItem.update { highlightedItem }
}
