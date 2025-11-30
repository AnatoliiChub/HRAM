package com.achub.hram.ble.model

sealed class BleConnectionsException : Exception() {
    data class DeviceNotConnectedException(override val message: String?) : BleConnectionsException()

    data class DisconnectRequestedException(override val message: String? = "") : BleConnectionsException()
}
