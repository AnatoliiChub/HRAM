package com.achub.hram.tracking

import com.achub.hram.models.DeviceModel

interface TrackingController {
    fun scan(id: String? = null)

    fun connectDevice(device: DeviceModel)

    fun disconnectDevice()

    fun startTracking()

    fun pauseTracking()

    fun finishTracking(name: String)

    fun cancelScanning()

    fun onAppForeground()

    fun clear()
}
