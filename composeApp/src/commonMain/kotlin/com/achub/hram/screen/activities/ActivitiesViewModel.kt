package com.achub.hram.screen.activities

import androidx.lifecycle.ViewModel
import com.achub.hram.data.HrActivityRepo
import com.achub.hram.stateInExt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class ActivitiesViewModel(val hrActivityRepo: HrActivityRepo) : ViewModel() {

    private val _uiState = hrActivityRepo.getActivitiesGraph()
        .map { list -> list.filter { it.activity.name.isNotEmpty() } }
        .map { ActivitiesUiState(it) }
        .flowOn(Dispatchers.Default)
    val uiState: StateFlow<ActivitiesUiState> = _uiState.stateInExt(initialValue = ActivitiesUiState(emptyList()))
}
