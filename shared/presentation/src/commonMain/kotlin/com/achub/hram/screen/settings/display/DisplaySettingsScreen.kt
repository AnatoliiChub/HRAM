package com.achub.hram.screen.settings.display

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.achub.hram.models.AppTheme
import com.achub.hram.style.Dimen16
import com.achub.hram.view.components.SettingsTagGroup
import hram.composeapp.generated.resources.Res
import hram.composeapp.generated.resources.theme_dark
import hram.composeapp.generated.resources.theme_light
import hram.composeapp.generated.resources.theme_system
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DisplaySettingsScreen() {
    val viewModel = koinViewModel<DisplaySettingsViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(Dimen16)
    ) {
        SettingsTagGroup(
            label = "Theme",
            options = AppTheme.entries,
            selectedOption = state.settings.theme,
            onOptionSelected = { viewModel.updateTheme(it) },
            optionLabel = { theme ->
                stringResource(
                    when (theme) {
                        AppTheme.System -> Res.string.theme_system
                        AppTheme.Dark -> Res.string.theme_dark
                        AppTheme.Light -> Res.string.theme_light
                    }
                )
            }
        )
    }
}
