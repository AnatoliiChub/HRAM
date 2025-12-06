package com.achub.hram.ble

import com.achub.hram.ble.model.HrNotification

interface BleParser {
    fun parseHrNotification(data: ByteArray): HrNotification

    fun parseBatteryLevel(data: ByteArray): Int
}
