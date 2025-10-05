package com.achub.hram.screen.activities

import androidx.lifecycle.ViewModel
import com.achub.hram.stateInExt
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class ActivitiesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ActivitiesUiState())
    val uiState = _uiState.stateInExt(initialValue = ActivitiesUiState())
}
