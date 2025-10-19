package com.achub.hram.tracking

import com.achub.hram.data.model.BleDevice
import com.achub.hram.data.model.Indications
import kotlinx.coroutines.flow.Flow

const val TRACKING_INIT_STATE = 0
const val ACTIVE_TRACKING_STATE = 1
const val PAUSED_TRACKING_STATE = 2

interface ActivityTrackingService {

    fun startTracking()
    fun pauseTracking()
    fun finishTracking()
    fun scan(onInit: () -> Unit, onUpdate: (List<BleDevice>) -> Unit, onComplete: () -> Unit)

    fun connect(
        device: BleDevice,
        onInitConnection: () -> Unit,
        onConnected: (BleDevice) -> Unit,
    )

    fun listen(): Flow<Indications>
    fun cancelScanning()
    fun disconect()
}