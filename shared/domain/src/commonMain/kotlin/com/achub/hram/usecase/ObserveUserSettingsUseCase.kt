package com.achub.hram.usecase

import com.achub.hram.data.state.SettingsStateRepo
import com.achub.hram.models.UserSettings
import kotlinx.coroutines.flow.Flow

class ObserveUserSettingsUseCase(private val settingsRepo: SettingsStateRepo) {
    operator fun invoke(): Flow<UserSettings> = settingsRepo.listen()
}
