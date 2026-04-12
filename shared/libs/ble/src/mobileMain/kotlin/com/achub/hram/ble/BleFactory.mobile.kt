package com.achub.hram.ble

import com.achub.hram.ble.core.connection.BleScanner
import com.achub.hram.ble.core.connection.HramBleScanner

internal actual fun createBleScanner(): BleScanner = HramBleScanner()

