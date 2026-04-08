package com.achub.hram.di

import com.achub.hram.data.HrActivityRepo
import com.achub.hram.data.state.BleStateRepo
import com.achub.hram.data.state.TrackingStateRepo
import com.achub.hram.tracking.ActivityTrackingManager
import com.achub.hram.tracking.BleConnectionOrchestrator
import com.achub.hram.tracking.BlePlatformStateHandler
import com.achub.hram.tracking.HramActivityTrackingManager
import com.achub.hram.tracking.HramBleConnectionOrchestrator
import com.achub.hram.tracking.HramSessionRecorder
import com.achub.hram.tracking.SessionRecorder
import com.achub.hram.tracking.stopwatch.HramStopWatch
import com.achub.hram.tracking.stopwatch.StopWatch
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [CoroutineModule::class])
@Configuration
class TrackingModule {
    @Single
    fun stopWatch(): StopWatch = HramStopWatch()

    @Single
    fun sessionRecorder(
        @WorkerThread dispatcher: CoroutineDispatcher,
        stopWatch: StopWatch,
        hrActivityRepo: HrActivityRepo,
        trackingStateRepo: TrackingStateRepo,
    ): SessionRecorder = HramSessionRecorder(dispatcher, stopWatch, hrActivityRepo, trackingStateRepo)

    @Single
    fun bleOrchestrator(
        @WorkerThread dispatcher: CoroutineDispatcher,
        platformStateHandler: BlePlatformStateHandler,
        bleStateRepo: BleStateRepo,
    ): BleConnectionOrchestrator = HramBleConnectionOrchestrator(dispatcher, platformStateHandler, bleStateRepo)

    @Single(binds = [ActivityTrackingManager::class])
    fun trackingManager(
        @WorkerThread dispatcher: CoroutineDispatcher,
        bleOrchestrator: BleConnectionOrchestrator,
        sessionRecorder: SessionRecorder,
    ): ActivityTrackingManager = HramActivityTrackingManager(dispatcher, bleOrchestrator, sessionRecorder)
}
