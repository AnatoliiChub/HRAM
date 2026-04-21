package com.achub.hram.di

import com.achub.hram.ActivityNameErrorMapper
import com.achub.hram.ext.BlePermissionController
import com.achub.hram.screen.activities.ActivitiesViewModel
import com.achub.hram.screen.record.RecordViewModel
import com.achub.hram.screen.settings.profile.UserProfileViewModel
import com.achub.hram.tracking.TrackingController
import com.achub.hram.usecase.DeleteActivitiesUseCase
import com.achub.hram.usecase.ExportCsvUseCase
import com.achub.hram.usecase.ObserveActivitiesUseCase
import com.achub.hram.usecase.ObserveBleStateUseCase
import com.achub.hram.usecase.ObserveTrackingStateUseCase
import com.achub.hram.usecase.RenameActivityUseCase
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.Module

@Module(includes = [CoroutineModule::class])
@Configuration
class ViewModelModule {
    @Factory
    @KoinViewModel
    fun recordViewModel(
        activityNameErrorMapper: ActivityNameErrorMapper,
        trackingController: TrackingController,
        observeBleState: ObserveBleStateUseCase,
        observeTrackingState: ObserveTrackingStateUseCase,
        @InjectedParam permissionsController: BlePermissionController,
        @WorkerThread dispatcher: CoroutineDispatcher,
    ) = RecordViewModel(
        activityNameErrorMapper = activityNameErrorMapper,
        permissionController = permissionsController,
        dispatcher = dispatcher,
        observeBleState = observeBleState,
        observeTrackingState = observeTrackingState,
        trackingController = trackingController,
    )

    @Factory
    @KoinViewModel
    fun activitiesViewModel(
        observeActivities: ObserveActivitiesUseCase,
        deleteActivities: DeleteActivitiesUseCase,
        renameActivity: RenameActivityUseCase,
        activityNameErrorMapper: ActivityNameErrorMapper,
        exportCsvUseCase: ExportCsvUseCase,
        @WorkerThread dispatcher: CoroutineDispatcher,
    ) = ActivitiesViewModel(
        observeActivities = observeActivities,
        deleteActivitiesUseCase = deleteActivities,
        renameActivity = renameActivity,
        activityNameErrorMapper = activityNameErrorMapper,
        exportCsvUseCase = exportCsvUseCase,
        dispatcher = dispatcher,
    )

    @Factory
    @KoinViewModel
    fun userProfileViewModel(
        settingsRepo: com.achub.hram.data.state.SettingsStateRepo
    ) = UserProfileViewModel(settingsRepo)
}
