package com.achub.hram.di

import com.achub.hram.tracking.ActivityTrackingManager
import com.achub.hram.tracking.BlePlatformStateHandler
import com.achub.hram.tracking.HramActivityTrackingManager
import com.achub.hram.tracking.stopwatch.HramStopWatch
import com.achub.hram.tracking.stopwatch.StopWatch
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [CoroutineModule::class])
@Configuration
class TrackingModule {
    @Single(binds = [ActivityTrackingManager::class])
    fun trackingManager(
        @WorkerThread dispatcher: CoroutineDispatcher,
        platformStateHandler: BlePlatformStateHandler,
    ) = HramActivityTrackingManager(dispatcher, platformStateHandler)

    @Single
    fun stopWatch(): StopWatch = HramStopWatch()
}
