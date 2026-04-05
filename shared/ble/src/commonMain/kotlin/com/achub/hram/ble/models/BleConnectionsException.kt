package com.achub.hram.ble.models

sealed class BleConnectionsException : Exception() {
    data class DeviceNotConnectedException(override val message: String?) : BleConnectionsException()

    data class DisconnectRequestedException(override val message: String? = "") : BleConnectionsException()

    /** Thrown when BLE is unavailable — e.g. Bluetooth is powered off or requirements are unmet. */
    class BleUnavailableException(cause: Throwable? = null) : BleConnectionsException() {
        override val message: String = "BLE is unavailable: ${cause?.message}"
    }
}
