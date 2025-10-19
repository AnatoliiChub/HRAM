package com.achub.hram.tracking

import com.achub.hram.data.model.BleDevice
import com.achub.hram.data.model.HrIndication
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow

const val TRACKING_INIT_STATE = 0
const val ACTIVE_TRACKING_STATE = 1
const val PAUSED_TRACKING_STATE = 2

interface ActivityTrackingService {

    val hrIndication: Channel<HrIndication>

    fun startTracking()
    fun pauseTracking()
    fun finishTracking()
    fun scan(onInit: () -> Unit, onUpdate: (List<BleDevice>) -> Unit, onComplete: () -> Unit)
    fun cancelScanning()
    fun disconnect()
    fun elapsedTime(): Flow<Long>
    fun connect(
        device: BleDevice,
        onInitConnection: () -> Unit,
        onConnected: (BleDevice) -> Unit,
    )
}