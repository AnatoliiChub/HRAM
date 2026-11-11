package com.achub.hram.tracking

import com.achub.hram.models.BleNotificationModel
import com.achub.hram.models.HrBucket

data class ActiveSessionBuckets(val durationMillis: Long, val buckets: List<HrBucket>)

interface SessionRecorder {
    fun startTracking()

    fun pauseTracking()

    fun finishTracking(name: String?)

    suspend fun record(notification: BleNotificationModel)

    fun releaseState()

    suspend fun trackingState(): TrackingStateStage

    suspend fun isTracking(): Boolean

    fun elapsedTime(): Long

    suspend fun currentBuckets(): ActiveSessionBuckets?

    fun cancelJobs()
}
