package com.achub.hram.data.models

import com.achub.hram.ble.models.BleDevice
import com.achub.hram.ble.models.BleNotification
import kotlinx.serialization.Serializable

@Serializable
sealed class BleState() {
    @Serializable
    sealed class Scanning : BleState() {
        @Serializable
        data class Update(val device: BleDevice) : Scanning()

        @Serializable
        data class Error(val error: ScanError, val timesStamp: Long) : Scanning()

        @Serializable
        data object Completed : Scanning()

        @Serializable
        data object Started : Scanning()
    }

    @Serializable
    data class Connecting(val device: BleDevice) : BleState()

    @Serializable
    data class Connected(val bleDevice: BleDevice, val error: ConnectionError? = null) : BleState()

    @Serializable
    data class NotificationUpdate(val bleNotification: BleNotification, val device: BleDevice) : BleState()

    @Serializable
    data object Disconnected : BleState()
}

@Serializable
enum class ScanError {
    BLUETOOTH_OFF,
    NO_BLE_PERMISSIONS,
}

enum class ConnectionError {
    UNKNOWN,
}
