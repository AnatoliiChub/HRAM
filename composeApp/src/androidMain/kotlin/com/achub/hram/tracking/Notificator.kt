package com.achub.hram.tracking

import android.app.Notification
import com.achub.hram.data.models.BleState
import kotlin.concurrent.atomics.ExperimentalAtomicApi

interface Notificator {
    fun createNotification(): Notification

    @OptIn(ExperimentalAtomicApi::class)
    suspend fun updateNotification(state: BleState, trackingStateStage: TrackingStateStage)
}
