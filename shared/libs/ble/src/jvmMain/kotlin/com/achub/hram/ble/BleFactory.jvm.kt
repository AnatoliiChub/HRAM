package com.achub.hram.ble

import com.achub.hram.ble.core.connection.BleScanner
import com.achub.hram.ble.core.connection.DesktopBleScanner

internal actual fun createBleScanner(): BleScanner = DesktopBleScanner()

