package com.achub.hram.screen.settings.display

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.achub.hram.data.state.SettingsStateRepo
import com.achub.hram.ext.launchIn
import com.achub.hram.models.AppTheme
import com.achub.hram.models.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DisplaySettingsUiState(
    val settings: UserSettings = UserSettings.Default
)

class DisplaySettingsViewModel(
    private val settingsRepo: SettingsStateRepo
) : ViewModel() {
    private val _uiState = MutableStateFlow(DisplaySettingsUiState())
    val uiState: StateFlow<DisplaySettingsUiState> = _uiState.asStateFlow()

    init {
        settingsRepo.listen()
            .onEach { settings -> _uiState.update { it.copy(settings = settings) } }
            .launchIn(viewModelScope)
    }

    fun updateTheme(theme: AppTheme) {
        viewModelScope.launch {
            settingsRepo.update(_uiState.value.settings.copy(theme = theme))
        }
    }
}
