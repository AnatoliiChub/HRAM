package com.achub.hram.di

import com.achub.hram.tracking.ActivityTrackingManager
import com.achub.hram.tracking.HramActivityTrackingManager
import com.achub.hram.tracking.HramStopWatch
import com.achub.hram.tracking.StopWatch
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class TrackingModule {

    @Single(binds = [ActivityTrackingManager::class])
    fun trackingManager() = HramActivityTrackingManager()

    @Single
    fun stopWatch(): StopWatch = HramStopWatch()
}