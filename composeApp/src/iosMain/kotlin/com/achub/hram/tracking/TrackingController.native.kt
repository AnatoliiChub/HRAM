package com.achub.hram.tracking

import com.achub.hram.ble.models.BleDevice

actual class TrackingController {
    actual fun scan(id: String?) {
    }

    actual fun connectDevice(device: BleDevice) {
    }

    actual fun disconnectDevice() {
    }

    actual fun startTracking() {
    }

    actual fun pauseTracking() {
    }

    actual fun finishTracking(name: String) {
    }

    actual fun cancelScanning() {
    }
}
