package com.achub.hram.screen.settings.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.achub.hram.data.state.SettingsStateRepo
import com.achub.hram.ext.launchIn
import com.achub.hram.models.BiologicalSex
import com.achub.hram.models.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UserProfileUiState(
    val settings: UserSettings = UserSettings.Default
)

class UserProfileViewModel(
    private val settingsRepo: SettingsStateRepo
) : ViewModel() {
    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    init {
        settingsRepo.listen()
            .onEach { settings -> _uiState.update { it.copy(settings = settings) } }
            .launchIn(viewModelScope)
    }

    fun updateBirthYear(year: Int) {
        viewModelScope.launch {
            settingsRepo.update(_uiState.value.settings.copy(birthYear = year))
        }
    }

    fun updateWeight(weight: Float) {
        viewModelScope.launch {
            settingsRepo.update(_uiState.value.settings.copy(weightKg = weight))
        }
    }

    fun updateHeight(height: Int) {
        viewModelScope.launch {
            settingsRepo.update(_uiState.value.settings.copy(heightCm = height))
        }
    }

    fun updateSex(sex: BiologicalSex) {
        viewModelScope.launch {
            settingsRepo.update(_uiState.value.settings.copy(biologicalSex = sex))
        }
    }
}
