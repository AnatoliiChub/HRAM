package com.achub.hram.tracking

import com.achub.hram.data.model.BleDevice
import com.achub.hram.data.model.HrNotifications
import kotlinx.coroutines.flow.Flow

const val TRACKING_INIT_STATE = 0
const val ACTIVE_TRACKING_STATE = 1
const val PAUSED_TRACKING_STATE = 2

interface TrackingManager {

    fun startTracking()
    fun pauseTracking()
    fun finishTracking()
    fun listenTrackingTime(): Flow<Long>
    fun scan(onInit: () -> Unit, onUpdate: (List<BleDevice>) -> Unit, onComplete: () -> Unit)
    fun listen(
        device: BleDevice,
        onInitConnection: () -> Unit,
        onConnected: (BleDevice) -> Unit,
        onNewIndications: (HrNotifications) -> Unit
    )

    fun cancelScanning()
    fun release()
}