package com.achub.hram

import com.achub.hram.data.models.BleState
import kotlinx.serialization.Serializable
import org.koin.ext.getFullName

/**
 * Enum representing the different BLE state types for simplified state representation
 * in Live Activities
 */
@Serializable
enum class BleStateType {
    SCANNING_STARTED,
    SCANNING_UPDATE,
    SCANNING_COMPLETED,
    SCANNING_ERROR,
    CONNECTING,
    CONNECTED,
    NOTIFICATION_UPDATE,
    DISCONNECTED;

    companion object {
        fun from(bleState: String): BleStateType {
            return when (bleState) {
                BleState.Scanning.Started::class.getFullName() -> SCANNING_STARTED
                BleState.Scanning.Update::class.getFullName() -> SCANNING_UPDATE
                BleState.Scanning.Completed::class.getFullName() -> SCANNING_COMPLETED
                BleState.Scanning.Error::class.getFullName() -> SCANNING_ERROR
                BleState.Connecting::class.getFullName() -> CONNECTING
                BleState.Connected::class.getFullName() -> CONNECTED
                BleState.NotificationUpdate::class.getFullName() -> NOTIFICATION_UPDATE
                BleState.Disconnected::class.getFullName() -> DISCONNECTED
                else -> throw IllegalArgumentException("Unknown BleState: $bleState")
            }
        }
    }
}
