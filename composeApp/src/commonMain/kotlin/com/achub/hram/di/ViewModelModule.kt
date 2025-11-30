package com.achub.hram.di

import com.achub.hram.data.HrActivityRepo
import com.achub.hram.screen.activities.ActivitiesViewModel
import com.achub.hram.screen.record.RecordViewModel
import com.achub.hram.tracking.HramActivityTrackingManager
import com.achub.hram.utils.ActivityNameValidation
import dev.icerock.moko.permissions.PermissionsController
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.Module

@Module
@Configuration
class ViewModelModule {
    @Factory
    @KoinViewModel
    fun recordViewModel(
        trackingManager: HramActivityTrackingManager,
        activityNameValidation: ActivityNameValidation,
        @InjectedParam permissionsController: PermissionsController
    ) = RecordViewModel(
        trackingManager = trackingManager,
        activityNameValidation = activityNameValidation,
        permissionController = permissionsController,
    )

    @Factory
    @KoinViewModel
    fun activitiesViewModel(
        hrActivityRepo: HrActivityRepo,
        activityNameValidation: ActivityNameValidation
    ) = ActivitiesViewModel(hrActivityRepo, activityNameValidation)
}
