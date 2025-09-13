package com.achub.hram.screen.activities

import cafe.adriel.voyager.core.model.screenModelScope
import com.achub.hram.screen.base.BaseActionHandler
import com.achub.hram.screen.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn

class ActivitiesViewModel : BaseViewModel<BaseActionHandler, ActivitiesUiState>() {
    private val _uiState = MutableStateFlow(ActivitiesUiState())
    override val uiState = _uiState.stateIn(
        scope = screenModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000),
        initialValue = ActivitiesUiState()
    )
    override var actionHandler: BaseActionHandler? = object : BaseActionHandler {}
}