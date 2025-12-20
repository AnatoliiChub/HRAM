package com.achub.hram.ble.core.data

import com.achub.hram.ble.models.HrNotification

interface BleParser {
    fun parseHrNotification(data: ByteArray): HrNotification

    fun parseBatteryLevel(data: ByteArray): Int
}
