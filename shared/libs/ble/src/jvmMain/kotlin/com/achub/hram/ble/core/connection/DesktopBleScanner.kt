package com.achub.hram.ble.core.connection

import com.achub.hram.ble.ObjcBridge
import com.achub.hram.ble.RealObjcBridge
import com.achub.hram.ble.models.BleConnectionsException.BleUnavailableException
import com.juul.kable.Advertisement
import com.juul.kable.Identifier
import com.juul.kable.UnmetRequirementException
import com.juul.kable.UnmetRequirementReason
import com.sun.jna.NativeLibrary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration
import kotlin.uuid.ExperimentalUuidApi

private const val POWER_STATE_ON = 1

/**
 * JVM/Desktop wrapper around [HramBleScanner] that checks IOBluetooth power state before
 * each targeted scan. Throws [UnmetRequirementException] with
 * [UnmetRequirementReason.BluetoothDisabled] if Bluetooth is off, matching the same exception
 * Kable raises on mobile so the existing error-handling pipeline works unchanged.
 */
@OptIn(ExperimentalUuidApi::class)
internal class DesktopBleScanner(
    private val delegate: BleScanner = HramBleScanner(),
    private val bridge: ObjcBridge = RealObjcBridge { NativeLibrary.getInstance("IOBluetooth") },
) : BleScanner {
    @OptIn(ExperimentalUuidApi::class)
    override suspend fun scan(identifier: Identifier, duration: Duration): Advertisement {
        return delegate.scan(identifier, duration)
    }

    override fun scan(): Flow<Advertisement> =
        if (!isBluetoothOn()) flow { throw BleUnavailableException() } else delegate.scan()

    private fun isBluetoothOn(): Boolean {
        // TODO SHOULD BE IMPLEMENTED FOR WINDOWS AND LINUX AS WELL
        val controller = bridge.invokePointer(
            "objc_msgSend",
            arrayOf(
                bridge.getClass("IOBluetoothHostController"),
                bridge.getSel("defaultController"),
            )
        )
        val powerState = bridge.invokeInt(
            "objc_msgSend",
            arrayOf(controller, bridge.getSel("powerState"))
        )
        return (powerState and 0xFF) == POWER_STATE_ON
    }
}
