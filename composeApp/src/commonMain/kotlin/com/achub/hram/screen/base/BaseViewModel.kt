package com.achub.hram.screen.base

import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.flow.StateFlow

abstract class BaseViewModel<A: BaseActionHandler, S: BaseUiState> : ScreenModel {

    abstract val uiState: StateFlow<S>

    abstract var actionHandler: A?

    override fun onDispose() {
        super.onDispose()
        actionHandler?.onDispose()
        actionHandler = null
    }
}

interface BaseActionHandler {
    fun onDispose() {}
}

interface BaseUiState