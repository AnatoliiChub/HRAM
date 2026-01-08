package com.achub.hram.tracking

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.achub.hram.data.models.BleState
import com.achub.hram.library.R
import kotlin.concurrent.atomics.ExperimentalAtomicApi

private const val NO_HEART_RATE = "--"
private const val HIGH_BATTERY_THRESHOLD = 75
private const val LOW_BATTERY_THRESHOLD = 25

class HramNotificator(
    private val ctx: Context,
    private val notificationManager: NotificationManager,
    private val trackingManager: ActivityTrackingManager,
) {
    fun createNotification(): Notification {
        val remoteViews = createRemoteViews()

        // Set initial state
        remoteViews.setViewVisibility(R.id.tvBleStatus, View.VISIBLE)
        remoteViews.setViewVisibility(R.id.heartRateValue, View.GONE)
        remoteViews.setViewVisibility(R.id.llDeviceInfoRow, View.GONE)
        remoteViews.setViewVisibility(R.id.trackingStatus, View.GONE)

        return NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setContentTitle(ctx.getString(R.string.notification_title))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(remoteViews)
            .setCustomBigContentView(remoteViews)
            .setSilent(true)
            .setOngoing(true)
            .build()
    }

    @OptIn(ExperimentalAtomicApi::class)
    suspend fun updateNotification(state: BleState) {
        val notificationData = when (state) {
            is BleState.Scanning -> handleScanning(state)

            is BleState.Connecting -> NotificationData(
                heartRate = null,
                heartIcon = R.drawable.ic_scanning_24,
                deviceName = state.device.name,
                bleStatus = ctx.getString(R.string.notification_connecting, state.device.name)
            )

            is BleState.Connected -> NotificationData(
                heartRate = NO_HEART_RATE,
                heartIcon = R.drawable.ic_heart,
                deviceName = state.bleDevice.name,
                isConnected = true,
                isContactOn = true,
                bleStatus = ctx.getString(R.string.notification_connected, state.bleDevice.name)
            )

            is BleState.NotificationUpdate -> handleNotificationUpdate(state)

            is BleState.Disconnected -> NotificationData(
                heartRate = null,
                heartIcon = R.drawable.ic_heart_disconnected,
                trackingStatus = null,
                deviceName = null,
                batteryLevel = null,
                isConnected = false,
                isContactOn = false,
                bleStatus = ctx.getString(R.string.notification_disconnected)
            )
        }

        updateNotification(notificationData)
    }

    private suspend fun handleNotificationUpdate(state: BleState.NotificationUpdate): NotificationData =
        with(state.bleNotification) {
            val isConnected = isBleConnected
            val isContactOn = hrNotification?.isContactOn ?: false
            val heartRate = if (isConnected && isContactOn) hrNotification.hrBpm.toString() else NO_HEART_RATE

            val heartIcon = when {
                !isConnected -> R.drawable.ic_heart_disconnected
                !isContactOn -> R.drawable.ic_heart_contact_off
                else -> R.drawable.ic_heart
            }
            val bleStatus = when {
                !isConnected -> ctx.getString(R.string.notification_connection_lost)
                !isContactOn -> ctx.getString(R.string.notification_no_contact)
                else -> ctx.getString(R.string.notification_connected_status)
            }

            val trackingStatus = ctx.getString(trackingManager.trackingState().text())

            NotificationData(
                heartRate = heartRate,
                heartIcon = heartIcon,
                trackingStatus = trackingStatus,
                deviceName = state.device.name,
                batteryLevel = batteryLevel,
                isConnected = isConnected,
                isContactOn = isContactOn,
                bleStatus = bleStatus
            )
        }

    private fun handleScanning(state: BleState.Scanning): NotificationData = when (state) {
        is BleState.Scanning.Started -> NotificationData(
            heartRate = null,
            heartIcon = R.drawable.ic_scanning_24,
            bleStatus = ctx.getString(R.string.notification_scanning_started)
        )

        is BleState.Scanning.Update -> NotificationData(
            heartRate = null,
            heartIcon = R.drawable.ic_scanning_24,
            deviceName = state.device.name,
            bleStatus = ctx.getString(R.string.notification_scanning_found_device, state.device.name)
        )

        is BleState.Scanning.Completed -> NotificationData(
            heartRate = null,
            heartIcon = R.drawable.ic_scanning_24,
            bleStatus = ctx.getString(R.string.notification_scanning_complete)
        )

        is BleState.Scanning.Error -> NotificationData(
            heartRate = null,
            heartIcon = R.drawable.ic_scanning_24,
            bleStatus = ctx.getString(R.string.notification_scanning_error, state.error)
        )
    }

    @OptIn(ExperimentalAtomicApi::class)
    private fun updateNotification(data: NotificationData) {
        val remoteViews = createRemoteViews()

        // Update heart rate display
        remoteViews.setTextViewText(R.id.heartRateValue, data.heartRate)

        // Update heart status icon
        val isHeatAnimated = data.heartIcon == R.drawable.ic_heart
        val animatedHeartVisibility = if (isHeatAnimated) View.VISIBLE else View.GONE
        val bleStatusIconVisibility = if (isHeatAnimated) View.GONE else View.VISIBLE

        remoteViews.setViewVisibility(R.id.pbAnimatedHeart, animatedHeartVisibility)
        remoteViews.setViewVisibility(R.id.ivBleStatusIcon, bleStatusIconVisibility)
        if (isHeatAnimated.not()) {
            remoteViews.setImageViewResource(R.id.ivBleStatusIcon, data.heartIcon)
        }
        // Update tracking status
        if (data.trackingStatus != null) {
            remoteViews.setTextViewText(R.id.trackingStatus, data.trackingStatus)
            remoteViews.setViewVisibility(R.id.trackingStatus, View.VISIBLE)
        } else {
            remoteViews.setViewVisibility(R.id.trackingStatus, View.INVISIBLE)
        }

        // Update device info row
        if (data.isConnected && data.deviceName != null) {
            remoteViews.setViewVisibility(R.id.llDeviceInfoRow, View.VISIBLE)
            remoteViews.setTextViewText(R.id.tvDeviceName, data.deviceName)

            // Update battery info
            if (data.batteryLevel != null) {
                val batteryText = ctx.getString(R.string.notification_battery_level, data.batteryLevel)
                remoteViews.setTextViewText(R.id.tvBatteryLevel, batteryText)

                val batteryIconRes = getBatteryIcon(data.batteryLevel)
                remoteViews.setImageViewResource(R.id.ivBattery, batteryIconRes)
            }
        } else {
            remoteViews.setViewVisibility(R.id.llDeviceInfoRow, View.GONE)
        }

        remoteViews.setTextViewText(R.id.tvBleStatus, data.bleStatus)

        val notification = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(remoteViews)
            .setCustomBigContentView(remoteViews)
            .setSilent(true)
            .setOngoing(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createRemoteViews(): RemoteViews {
        return RemoteViews(ctx.packageName, R.layout.notification_hr_tracking)
    }

    private fun getBatteryIcon(level: Int) = when {
        level > HIGH_BATTERY_THRESHOLD -> R.drawable.ic_battery_full_green
        level > LOW_BATTERY_THRESHOLD -> R.drawable.ic_battery_half_green
        else -> R.drawable.ic_battery_low_red
    }
}

data class NotificationData(
    val heartRate: String? = null,
    val heartIcon: Int,
    val trackingStatus: String? = null,
    val deviceName: String? = null,
    val batteryLevel: Int? = null,
    val isConnected: Boolean = false,
    val isContactOn: Boolean = false,
    val bleStatus: String
)

fun TrackingStateStage.text() = when (this) {
    TrackingStateStage.TRACKING_INIT_STATE -> R.string.notification_no_tracking
    TrackingStateStage.ACTIVE_TRACKING_STATE -> R.string.notification_tracking
    TrackingStateStage.PAUSED_TRACKING_STATE -> R.string.notification_tracking_paused
}
