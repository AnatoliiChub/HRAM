package com.achub.hram.di

import com.achub.hram.data.repo.state.BleStateRepo
import com.achub.hram.data.repo.HrActivityRepo
import com.achub.hram.data.repo.state.TrackingStateRepo
import com.achub.hram.screen.activities.ActivitiesViewModel
import com.achub.hram.screen.record.RecordViewModel
import com.achub.hram.tracking.TrackingController
import com.achub.hram.utils.ActivityNameErrorMapper
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
        activityNameErrorMapper: ActivityNameErrorMapper,
        trackingController: TrackingController,
        bleStateRepo: BleStateRepo,
        trackingStateRepo: TrackingStateRepo,
        @InjectedParam permissionsController: PermissionsController,
        @WorkerThread dispatcher: CoroutineDispatcher,
    ) = RecordViewModel(
        activityNameErrorMapper = activityNameErrorMapper,
        permissionController = permissionsController,
        dispatcher = dispatcher,
        bleStateRepo = bleStateRepo,
        trackingStateRepo = trackingStateRepo,
        trackingController = trackingController,
    )

    @Factory
    @KoinViewModel
    fun activitiesViewModel(
        hrActivityRepo: HrActivityRepo,
        activityNameErrorMapper: ActivityNameErrorMapper,
        @WorkerThread dispatcher: CoroutineDispatcher,
    ) = ActivitiesViewModel(hrActivityRepo, activityNameErrorMapper, dispatcher)
}
