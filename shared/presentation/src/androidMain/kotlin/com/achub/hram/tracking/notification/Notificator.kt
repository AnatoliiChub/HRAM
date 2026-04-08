package com.achub.hram.tracking.notification

import android.app.Notification
import com.achub.hram.models.BleState
import com.achub.hram.tracking.TrackingStateStage
import kotlin.concurrent.atomics.ExperimentalAtomicApi

interface Notificator {
    fun createNotification(): Notification

    @OptIn(ExperimentalAtomicApi::class)
    suspend fun updateNotification(state: BleState, trackingStateStage: TrackingStateStage)
}
