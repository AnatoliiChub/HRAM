package com.achub.hram.ble

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single
import platform.CoreBluetooth.CBCentralManager
import platform.CoreBluetooth.CBCentralManagerDelegateProtocol
import platform.CoreBluetooth.CBCentralManagerOptionShowPowerAlertKey
import platform.CoreBluetooth.CBManagerStatePoweredOn
import platform.darwin.NSObject

//Suppress "turn on bluetooth" popup
private val options = mapOf<Any?, Any>(CBCentralManagerOptionShowPowerAlertKey to false)

@Single
class BluetoothStateIos : BluetoothState {

    private var manager: CBCentralManager? = null
    private var delegate: NSObject? = null

    override val isBluetoothOn = MutableStateFlow(manager?.state == CBManagerStatePoweredOn)

    override fun init() {
        val delegate = object : NSObject(), CBCentralManagerDelegateProtocol {
            override fun centralManagerDidUpdateState(central: CBCentralManager) {
                isBluetoothOn.update { central.state == CBManagerStatePoweredOn }
            }
        }
        manager = CBCentralManager(delegate, null, options)
        this.delegate = delegate

    }

    override fun release() {
        manager = null
        delegate = null
    }
}
