package com.achub.hram.screen.activities

import cafe.adriel.voyager.core.model.ScreenModel
import com.achub.hram.stateInExt
import kotlinx.coroutines.flow.MutableStateFlow

class ActivitiesViewModel : ScreenModel {
    private val _uiState = MutableStateFlow(ActivitiesUiState())
    val uiState = _uiState.stateInExt(initialValue = ActivitiesUiState())
}