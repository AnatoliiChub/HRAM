package com.achub.hram.screen.activities

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn

class ActivitiesViewModel : ScreenModel {
    private val _uiState = MutableStateFlow(ActivitiesUiState())
    val uiState = _uiState.stateIn(
        scope = screenModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000),
        initialValue = ActivitiesUiState()
    )
}