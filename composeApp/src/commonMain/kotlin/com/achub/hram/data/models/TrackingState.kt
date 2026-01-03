package com.achub.hram.data.models

import com.achub.hram.ble.models.BleDevice
import com.achub.hram.ble.models.BleNotification
import kotlinx.serialization.Serializable

@Serializable
sealed class TrackingState {
    @Serializable
    data class Scanning(val devices: List<BleDevice>, val completed: Boolean = false, val error: ScanError? = null) :
        TrackingState()

    @Serializable
    data class Connecting(val device: BleDevice) : TrackingState()

    @Serializable
    data class Connected(val bleDevice: BleDevice, val error: ConnectionError? = null) : TrackingState()

    @Serializable
    data class NotificationUpdate(val bleNotification: BleNotification) : TrackingState()

    @Serializable
    data object Disconnected : TrackingState()
}

enum class ScanError {
    BLUETOOTH_OFF,
}

enum class ConnectionError {
    UNKNOWN,
}
