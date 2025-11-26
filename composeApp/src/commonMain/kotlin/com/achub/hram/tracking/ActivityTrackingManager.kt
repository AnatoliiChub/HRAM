package com.achub.hram.tracking

import com.achub.hram.data.models.BleDevice
import com.achub.hram.data.models.BleIndication
import kotlinx.coroutines.channels.Channel

const val TRACKING_INIT_STATE = 0
const val ACTIVE_TRACKING_STATE = 1
const val PAUSED_TRACKING_STATE = 2

interface ActivityTrackingManager {

    val bleIndication: Channel<BleIndication>

    fun startTracking()
    fun pauseTracking()
    fun finishTracking(name: String?)
    fun scan(onInit: () -> Unit, onUpdate: (List<BleDevice>) -> Unit, onComplete: () -> Unit)
    fun cancelScanning()
    fun disconnect()
    fun connect(
        device: BleDevice,
        onInitConnection: () -> Unit,
        onConnected: (BleDevice) -> Unit,
    )
}