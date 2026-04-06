package com.achub.hram.tracking

import android.content.Context
import android.content.Intent
import com.achub.hram.models.DeviceModel
import com.achub.hram.tracking.notification.ACTION
import com.achub.hram.tracking.notification.BleTrackingService

class AndroidTrackingController(
    val context: Context,
) : TrackingController {
    override fun scan(id: String?) {
        context.startForegroundService(
            Intent(context, BleTrackingService::class.java).apply {
                putExtra(ACTION, Action.Scan.ordinal)
            }
        )
    }

    override fun connectDevice(device: DeviceModel) {
        context.startForegroundService(
            Intent(context, BleTrackingService::class.java).apply {
                putExtra(ACTION, Action.Connect.ordinal)
                putExtra(BleTrackingService.EXTRA_DEVICE_ID, device.identifier)
                putExtra(BleTrackingService.EXTRA_DEVICE_NAME, device.name)
            }
        )
    }

    override fun disconnectDevice() {
        context.startForegroundService(
            Intent(context, BleTrackingService::class.java).apply {
                putExtra(ACTION, Action.Disconnect.ordinal)
            }
        )
    }

    override fun startTracking() {
        context.startForegroundService(
            Intent(context, BleTrackingService::class.java).apply {
                putExtra(ACTION, Action.StartTracking.ordinal)
            }
        )
    }

    override fun pauseTracking() {
        context.startForegroundService(
            Intent(context, BleTrackingService::class.java).apply {
                putExtra(ACTION, Action.PauseTracking.ordinal)
            }
        )
    }

    override fun finishTracking(name: String) {
        context.startForegroundService(
            Intent(context, BleTrackingService::class.java).apply {
                putExtra(ACTION, Action.StopTracking.ordinal)
                putExtra(BleTrackingService.EXTRA_ACTIVITY_NAME, name)
            }
        )
    }

    override fun cancelScanning() {
        context.startForegroundService(
            Intent(context, BleTrackingService::class.java).apply {
                putExtra(ACTION, Action.CancelScanning.ordinal)
            }
        )
    }

    override fun clear() {
        // No-op for Android
    }

    override fun onAppForeground() {
        // No-op for Android
    }
}
