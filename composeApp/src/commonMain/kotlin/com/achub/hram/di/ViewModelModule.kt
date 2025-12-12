package com.achub.hram.di

import com.achub.hram.data.HrActivityRepo
import com.achub.hram.screen.activities.ActivitiesViewModel
import com.achub.hram.screen.record.RecordViewModel
import com.achub.hram.tracking.HramActivityTrackingManager
import com.achub.hram.utils.ActivityNameValidation
import dev.icerock.moko.permissions.PermissionsController
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
        trackingManager: HramActivityTrackingManager,
        activityNameValidation: ActivityNameValidation,
        @InjectedParam permissionsController: PermissionsController,
        @WorkerThread dispatcher: CoroutineDispatcher,
    ) = RecordViewModel(
        trackingManager = trackingManager,
        activityNameValidation = activityNameValidation,
        permissionController = permissionsController,
        dispatcher = dispatcher,
    )

    @Factory
    @KoinViewModel
    fun activitiesViewModel(
        hrActivityRepo: HrActivityRepo,
        activityNameValidation: ActivityNameValidation,
        @WorkerThread dispatcher: CoroutineDispatcher,
    ) = ActivitiesViewModel(hrActivityRepo, activityNameValidation, dispatcher)
}
