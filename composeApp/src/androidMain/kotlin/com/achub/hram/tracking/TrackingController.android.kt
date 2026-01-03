package com.achub.hram.tracking

import android.content.Context
import android.content.Intent
import com.achub.hram.ble.models.BleDevice

actual class TrackingController(
    val context: Context,
) {
    actual fun scan(id: String?) {
        context.startForegroundService(
            Intent(context, BleTrackingService::class.java).apply {
                putExtra(ACTION, BleTrackingService.Action.Scan.ordinal)
            }
        )
    }

    actual fun connectDevice(device: BleDevice) {
        context.startForegroundService(
            Intent(context, BleTrackingService::class.java).apply {
                putExtra(ACTION, BleTrackingService.Action.Connect.ordinal)
                putExtra(BleTrackingService.EXTRA_DEVICE, device.identifier)
            }
        )
    }

    actual fun disconnectDevice() {
        context.startForegroundService(
            Intent(context, BleTrackingService::class.java).apply {
                putExtra(ACTION, BleTrackingService.Action.Disconnect.ordinal)
            }
        )
    }

    actual fun startTracking() {
        context.startForegroundService(
            Intent(context, BleTrackingService::class.java).apply {
                putExtra(ACTION, BleTrackingService.Action.StartTracking.ordinal)
            }
        )
    }

    actual fun pauseTracking() {
        context.startForegroundService(
            Intent(context, BleTrackingService::class.java).apply {
                putExtra(ACTION, BleTrackingService.Action.PauseTracking.ordinal)
            }
        )
    }

    actual fun stopTracking(name: String) {
        context.startForegroundService(
            Intent(context, BleTrackingService::class.java).apply {
                putExtra(ACTION, BleTrackingService.Action.StopTracking.ordinal)
                putExtra(BleTrackingService.EXTRA_NAME, name)
            }
        )
    }
}
