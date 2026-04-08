package com.achub.hram.models

import kotlinx.serialization.Serializable

@Serializable
sealed class BleState() {
    @Serializable
    sealed class Scanning : BleState() {
        @Serializable
        data class Update(val device: DeviceModel) : Scanning()

        @Serializable
        data class Error(val error: ScanError, val timesStamp: Long) : Scanning()

        @Serializable
        data object Completed : Scanning()

        @Serializable
        data object Started : Scanning()
    }

    @Serializable
    data class Connecting(val device: DeviceModel) : BleState()

    @Serializable
    data class Connected(val bleDevice: DeviceModel, val error: ConnectionError? = null) : BleState()

    @Serializable
    data class NotificationUpdate(val bleNotification: BleNotificationModel, val device: DeviceModel) : BleState()

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
