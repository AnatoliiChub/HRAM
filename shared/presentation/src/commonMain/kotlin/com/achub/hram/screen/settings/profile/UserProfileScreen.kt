package com.achub.hram.screen.settings.profile

import androidx.compose.foundation.background
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
import hram.composeapp.generated.resources.Res
import hram.composeapp.generated.resources.sex_female
import hram.composeapp.generated.resources.sex_male
import hram.composeapp.generated.resources.sex_other
import hram.composeapp.generated.resources.user_profile_biological_sex
import hram.composeapp.generated.resources.user_profile_birth_year
import hram.composeapp.generated.resources.user_profile_height
import hram.composeapp.generated.resources.user_profile_weight
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
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
            .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
            .padding(Dimen16),
        verticalArrangement = Arrangement.spacedBy(Dimen16)
    ) {
        SettingsTagGroup(
            label = stringResource(Res.string.user_profile_biological_sex),
            options = BiologicalSex.entries,
            selectedOption = state.settings.biologicalSex,
            onOptionSelected = { viewModel.updateSex(it) },
            optionLabel = { sex ->
                stringResource(
                    when (sex) {
                        BiologicalSex.Male -> Res.string.sex_male
                        BiologicalSex.Female -> Res.string.sex_female
                        BiologicalSex.Other -> Res.string.sex_other
                    }
                )
            }
        )

        SettingsRangePicker(
            label = stringResource(Res.string.user_profile_birth_year, state.settings.birthYear.toString()),
            value = state.settings.birthYear.toFloat(),
            range = MIN_BIRTH_YEAR..currentYear,
            onValueChange = { viewModel.updateBirthYear(it.roundToInt()) }
        )

        SettingsRangePicker(
            label = stringResource(Res.string.user_profile_weight, state.settings.weightKg.roundToInt().toString()),
            value = state.settings.weightKg,
            range = MIN_WEIGHT..MAX_WEIGHT,
            onValueChange = { viewModel.updateWeight(it) }
        )

        SettingsRangePicker(
            label = stringResource(Res.string.user_profile_height, state.settings.heightCm.toString()),
            value = state.settings.heightCm.toFloat(),
            range = MIN_HEIGHT..MAX_HEIGHT,
            onValueChange = { viewModel.updateHeight(it.roundToInt()) }
        )
    }
}
