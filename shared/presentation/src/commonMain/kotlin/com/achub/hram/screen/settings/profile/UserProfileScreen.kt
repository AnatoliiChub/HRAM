package com.achub.hram.screen.settings.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.achub.hram.models.BiologicalSex
import com.achub.hram.style.Dimen16
import com.achub.hram.view.components.SettingsRangePicker
import com.achub.hram.view.components.SettingsTagGroup
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt
import kotlin.time.Clock

private const val MIN_BIRTH_YEAR = 1900f
private const val MIN_WEIGHT = 25f
private const val MAX_WEIGHT = 250f
private const val MIN_HEIGHT = 50f
private const val MAX_HEIGHT = 250f

@Composable
fun UserProfileScreen() {
    val viewModel = koinViewModel<UserProfileViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentYear = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year.toFloat()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimen16),
        verticalArrangement = Arrangement.spacedBy(Dimen16)
    ) {
        SettingsTagGroup(
            label = "Biological Sex",
            options = BiologicalSex.entries,
            selectedOption = state.settings.biologicalSex,
            onOptionSelected = { viewModel.updateSex(it) },
            optionLabel = { it.name }
        )

        SettingsRangePicker(
            label = "Birth Year: ${state.settings.birthYear}",
            value = state.settings.birthYear.toFloat(),
            range = MIN_BIRTH_YEAR..currentYear,
            onValueChange = { viewModel.updateBirthYear(it.roundToInt()) }
        )

        SettingsRangePicker(
            label = "Weight: ${state.settings.weightKg.roundToInt()} kg",
            value = state.settings.weightKg,
            range = MIN_WEIGHT..MAX_WEIGHT,
            onValueChange = { viewModel.updateWeight(it) }
        )

        SettingsRangePicker(
            label = "Height: ${state.settings.heightCm} cm",
            value = state.settings.heightCm.toFloat(),
            range = MIN_HEIGHT..MAX_HEIGHT,
            onValueChange = { viewModel.updateHeight(it.roundToInt()) }
        )
    }
}
