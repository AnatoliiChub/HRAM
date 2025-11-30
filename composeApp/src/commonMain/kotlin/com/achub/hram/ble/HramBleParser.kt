package com.achub.hram.ble

import com.achub.hram.ble.model.HrNotification
import com.achub.hram.ext.uint16
import com.achub.hram.ext.uint8

// flag bit masks (Heart Rate Measurement characteristic)
private const val BIT_FORMAT_UINT8 = 0x01
private const val BIT_SENSOR_CONTACT_DETECTED = 0x02
private const val BIT_SENSOR_CONTACT_SUPPORTED = 0x04

// byte offsets inside notification
private const val OFFSET_FLAGS = 0
private const val OFFSET_HEART_RATE_VALUE = 1

class HramBleParser() : BleParser {
    override fun parseHrNotification(data: ByteArray): HrNotification {
        val flags = data.uint8(OFFSET_FLAGS)
        val is8bitFormat = flags and BIT_FORMAT_UINT8 == 0
        val isSensorContactSupported = flags and BIT_SENSOR_CONTACT_SUPPORTED > 0
        val sensorContacted = if (isSensorContactSupported) flags and BIT_SENSOR_CONTACT_DETECTED > 0 else true
        val heartRate = if (is8bitFormat) {
            data.uint8(OFFSET_HEART_RATE_VALUE)
        } else {
            data.uint16(OFFSET_HEART_RATE_VALUE)
        }
        return HrNotification(
            hrBpm = heartRate,
            isSensorContactSupported = isSensorContactSupported,
            isContactOn = sensorContacted
        )
    }

    override fun parseBatteryLevel(data: ByteArray) = data.uint8(0)
}
