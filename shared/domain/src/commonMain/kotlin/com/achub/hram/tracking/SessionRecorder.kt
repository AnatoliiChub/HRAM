package com.achub.hram.tracking

import com.achub.hram.models.BleNotificationModel

interface SessionRecorder {
    fun startTracking()

    fun pauseTracking()

    fun finishTracking(name: String?)

    suspend fun record(notification: BleNotificationModel)

    fun releaseState()

    suspend fun trackingState(): TrackingStateStage

    suspend fun isTracking(): Boolean

    fun elapsedTime(): Long

    fun cancelJobs()
}
