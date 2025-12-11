package com.achub.hram.tracking

import com.achub.hram.ble.model.BleDevice
import com.achub.hram.ble.model.BleNotification
import kotlinx.coroutines.flow.StateFlow

const val TRACKING_INIT_STATE = 0
const val ACTIVE_TRACKING_STATE = 1
const val PAUSED_TRACKING_STATE = 2

interface ActivityTrackingManager {
    val bleNotification: StateFlow<BleNotification>

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
