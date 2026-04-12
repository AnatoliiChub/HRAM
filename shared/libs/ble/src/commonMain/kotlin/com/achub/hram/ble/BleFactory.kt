package com.achub.hram.ble

import com.achub.hram.ble.core.connection.BleConnector
import com.achub.hram.ble.core.connection.BleScanner
import com.achub.hram.ble.core.connection.ConnectionTracker
import com.achub.hram.ble.core.connection.HramBleConnectionManager
import com.achub.hram.ble.core.connection.HramBleConnector
import com.achub.hram.ble.core.connection.HramConnectionTracker
import com.achub.hram.ble.core.data.BleDataRepo
import com.achub.hram.ble.core.data.BleParser
import com.achub.hram.ble.core.data.HramBleDataRepo
import com.achub.hram.ble.core.data.HramBleParser
import com.achub.hram.ble.models.HramPeripheralConvertor
import com.achub.hram.ble.models.PeripheralConvertor
import kotlinx.coroutines.CoroutineScope

/**
 * Factory object that constructs all BLE components.
 *
 * Hides internal `Hram*` implementation classes from external modules — callers only see
 * the public interface types. Renaming or restructuring internals in `:ble` requires changes
 * only here, not in consuming modules.
 */
object BleFactory {
    fun scanner(): BleScanner = createBleScanner()

    fun connector(): BleConnector = HramBleConnector()

    fun parser(): BleParser = HramBleParser()

    fun dataRepo(parser: BleParser): BleDataRepo = HramBleDataRepo(parser)

    fun connectionTracker(bluetoothState: BluetoothState): ConnectionTracker =
        HramConnectionTracker(bluetoothState)

    fun peripheralConvertor(): PeripheralConvertor = HramPeripheralConvertor()

    fun hrDeviceRepo(
        scope: CoroutineScope,
        connectionTracker: ConnectionTracker,
        bleDataRepo: BleDataRepo,
        bleScanner: BleScanner,
        bleConnector: BleConnector,
        peripheralConvertor: PeripheralConvertor,
    ): HrDeviceRepo = HramHrDeviceRepo(
        bleDataRepo = bleDataRepo,
        bleConnectionManager = HramBleConnectionManager(
            connectionTracker = connectionTracker,
            scanner = bleScanner,
            connector = bleConnector,
            peripheralConverter = peripheralConvertor,
            scope = scope,
        ),
    )
}

internal expect fun createBleScanner(): BleScanner
