@file:Suppress("ktlint:standard:filename", "detekt:Filename")

package com.achub.hram.ble

import com.achub.hram.ext.logger
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import platform.CoreBluetooth.CBCentralManager
import platform.CoreBluetooth.CBCentralManagerDelegateProtocol
import platform.CoreBluetooth.CBCentralManagerOptionShowPowerAlertKey
import platform.CoreBluetooth.CBManagerStatePoweredOn
import platform.darwin.NSObject

// Suppress "turn on bluetooth" popup
private val options = mapOf<Any?, Any>(CBCentralManagerOptionShowPowerAlertKey to false)

private const val TAG = "BluetoothStateIos"

class BluetoothStateIos : BluetoothState {
    private var manager: CBCentralManager? = null
    private var delegate: CBCentralManagerDelegateProtocol? = null

    override val isBluetoothOn = callbackFlow {
        delegate = object : NSObject(), CBCentralManagerDelegateProtocol {
            override fun centralManagerDidUpdateState(central: CBCentralManager) {
                trySendBlocking(central.state == CBManagerStatePoweredOn)
                    .onFailure { logger(TAG) { "BluetoothState: failed: $it" } }
            }
        }
        manager = CBCentralManager(delegate, null, options)

        awaitClose {
            manager = null
            delegate = null
        }
    }
}
