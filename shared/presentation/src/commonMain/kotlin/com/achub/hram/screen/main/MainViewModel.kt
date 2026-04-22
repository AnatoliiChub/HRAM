package com.achub.hram.screen.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.achub.hram.ext.launchIn
import com.achub.hram.models.AppTheme
import com.achub.hram.usecase.ObserveUserSettingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

data class MainUiState(
    val theme: AppTheme = AppTheme.System
)

class MainViewModel(
    private val observeUserSettings: ObserveUserSettingsUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        observeUserSettings()
            .onEach { settings ->
                _uiState.update { it.copy(theme = settings.theme) }
            }
            .launchIn(viewModelScope)
    }
}
