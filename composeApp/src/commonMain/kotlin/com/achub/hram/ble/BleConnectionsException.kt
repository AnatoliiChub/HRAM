package com.achub.hram.ble

sealed class BleConnectionsException : Exception() {
    data class DeviceNotConnectedException(override val message: String?) : BleConnectionsException()

    data object DisconnectRequestedException : BleConnectionsException()
}