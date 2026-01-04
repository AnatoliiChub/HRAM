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
                putExtra(BleTrackingService.EXTRA_DEVICE_ID, device.identifier)
                putExtra(BleTrackingService.EXTRA_DEVICE_NAME, device.name)
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

    actual fun finishTracking(name: String) {
        context.startForegroundService(
            Intent(context, BleTrackingService::class.java).apply {
                putExtra(ACTION, BleTrackingService.Action.StopTracking.ordinal)
                putExtra(BleTrackingService.EXTRA_ACTIVITY_NAME, name)
            }
        )
    }

    actual fun cancelScanning() {
        context.startForegroundService(
            Intent(context, BleTrackingService::class.java).apply {
                putExtra(ACTION, BleTrackingService.Action.CancelScanning.ordinal)
            }
        )
    }
}
