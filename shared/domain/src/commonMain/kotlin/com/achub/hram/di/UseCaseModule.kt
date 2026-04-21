package com.achub.hram.di

import com.achub.hram.data.HrActivityRepo
import com.achub.hram.data.state.BleStateRepo
import com.achub.hram.data.state.SettingsStateRepo
import com.achub.hram.data.state.TrackingStateRepo
import com.achub.hram.export.FileExporter
import com.achub.hram.usecase.CalculateCaloriesUseCase
import com.achub.hram.usecase.DeleteActivitiesUseCase
import com.achub.hram.usecase.ExportCsvUseCase
import com.achub.hram.usecase.ObserveActivitiesUseCase
import com.achub.hram.usecase.ObserveBleStateUseCase
import com.achub.hram.usecase.ObserveTrackingStateUseCase
import com.achub.hram.usecase.RenameActivityUseCase
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Module
@Configuration
class UseCaseModule {
    @Factory
    fun observeTrackingState(repo: TrackingStateRepo) = ObserveTrackingStateUseCase(repo)

    @Factory
    fun observeBleState(repo: BleStateRepo) = ObserveBleStateUseCase(repo)

    @Factory
    fun calculateCalories() = CalculateCaloriesUseCase()

    @Factory
    fun observeActivities(
        repo: HrActivityRepo,
        settingsRepo: SettingsStateRepo,
        calculateCaloriesUseCase: CalculateCaloriesUseCase
    ) = ObserveActivitiesUseCase(repo, settingsRepo, calculateCaloriesUseCase)

    @Factory
    fun deleteActivities(repo: HrActivityRepo) = DeleteActivitiesUseCase(repo)

    @Factory
    fun renameActivity(repo: HrActivityRepo) = RenameActivityUseCase(repo)

    @Factory
    fun provideExportCsvUseCase(
        hrActivityRepo: HrActivityRepo,
        fileExporter: FileExporter,
    ) = ExportCsvUseCase(hrActivityRepo, fileExporter)
}
