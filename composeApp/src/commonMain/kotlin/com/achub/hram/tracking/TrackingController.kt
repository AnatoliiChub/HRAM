package com.achub.hram.tracking

import com.achub.hram.ble.models.BleDevice

expect class TrackingController {
    fun scan(id: String? = null)

    fun connectDevice(device: BleDevice)

    fun disconnectDevice()

    fun startTracking()

    fun pauseTracking()

    fun finishTracking(name: String)

    fun cancelScanning()
}
