package com.achub.hram.di

import com.achub.hram.tracking.ActivityTrackingManager
import com.achub.hram.tracking.HramActivityTrackingManager
import com.achub.hram.tracking.HramStopWatch
import com.achub.hram.tracking.StopWatch
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [CoroutineModule::class])
@Configuration
class TrackingModule {
    @Single(binds = [ActivityTrackingManager::class])
    fun trackingManager(@WorkerThread dispatcher: CoroutineDispatcher) = HramActivityTrackingManager(dispatcher)

    @Single
    fun stopWatch(): StopWatch = HramStopWatch()
}
