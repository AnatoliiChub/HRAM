package com.achub.hram.tracking

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
import android.graphics.drawable.Icon
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.achub.hram.BLE_SCAN_DURATION
import com.achub.hram.ble.models.HramBleDevice
import com.achub.hram.data.models.BleState
import com.achub.hram.di.CoroutineModule.Companion.WORKER_DISPATCHER
import com.achub.hram.ext.launchIn
import com.achub.hram.ext.logger
import com.achub.hram.library.R
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinComponent
import org.koin.core.qualifier.named
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.milliseconds

const val CHANNEL_ID = "BLE_TRACKING_CHANNEL_ID"
const val ACTION = "com.achub.hram.tracking.BleTrackingService.ACTION"
const val NOTIFICATION_ID = 1
private const val TAG = "BleTrackingService"

class BleTrackingService : Service(), KoinComponent {
    companion object {
        const val EXTRA_DEVICE_ID = "device_id"
        const val EXTRA_DEVICE_NAME = "device_name"
        const val EXTRA_ACTIVITY_NAME = "activity_name"
    }

    private val tracker: ActivityTrackingManager by inject()
    private val dispatcher: CoroutineDispatcher by inject(qualifier = named(WORKER_DISPATCHER))
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private var hrTrackingJob: Job? = null
    private val currentAction = AtomicInteger(-1)
    private var scanJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        val channel = getString(R.string.channel_name)
        val notificationChannel = NotificationChannel(CHANNEL_ID, channel, NotificationManager.IMPORTANCE_DEFAULT)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(notificationChannel)
        val notification: Notification = createNotification()
        ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE)
        scope.launch { tracker }
        logger(TAG) { "Service created" }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logger(TAG) { "onStartCommand with action: $intent $dispatcher" }
        if (intent == null) return START_STICKY
        currentAction.set(Action.entries[intent.getIntExtra(ACTION, -1)].ordinal)
        val action = Action.entries[currentAction.get()]
        logger(TAG) { "Processing action: $action" }

        when (action) {
            Action.CancelScanning -> tracker.cancelScanning()
            Action.Scan -> scan()
            Action.Connect -> connect(intent)
            Action.Disconnect -> tracker.disconnect()
            Action.StartTracking -> scope.launch { tracker.startTracking() }
            Action.PauseTracking -> scope.launch { tracker.pauseTracking() }
            Action.StopTracking -> stopTracking(intent)
        }
        return START_STICKY
    }

    private fun stopTracking(intent: Intent) =
        scope.launch { tracker.finishTracking(intent.getStringExtra(EXTRA_ACTIVITY_NAME)) }

    private fun connect(intent: Intent) {
        intent.getStringExtra(EXTRA_DEVICE_ID)?.let { identifier ->
            val device = HramBleDevice(name = intent.getStringExtra(EXTRA_DEVICE_NAME) ?: "", identifier = identifier)
            tracker.connectAndSubscribe(device = device)
                .launchIn(scope)
                .let { hrTrackingJob = it }
        }
    }

    @OptIn(FlowPreview::class)
    private fun scan() {
        tracker.scan(BLE_SCAN_DURATION.milliseconds)
            .filter { currentAction.get() == Action.Scan.ordinal }
            .flowOn(dispatcher)
            .launchIn(scope)
            .let { scanJob = it }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        logger(TAG) { "Service destroyed" }
        hrTrackingJob?.cancel()
        scanJob?.cancel()
        hrTrackingJob = null
        scanJob = null
        scope.cancel()
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setSilent(true)
            .setOngoing(true)
            .build()
    }

    private suspend fun updateNotification(state: BleState) {
        val notificationData = when (state) {
            is BleState.Scanning -> when (state) {
                is BleState.Scanning.Started -> NotificationData(
                    getString(R.string.notification_scanning_started),
                    R.drawable.ic_scanning_24
                )

                is BleState.Scanning.Update -> NotificationData(
                    getString(R.string.notification_scanning_found_device, state.device.name),
                    R.drawable.ic_scanning_24
                )

                is BleState.Scanning.Completed -> NotificationData(
                    getString(R.string.notification_scanning_complete),
                    R.drawable.ic_scanning_24
                )

                is BleState.Scanning.Error -> NotificationData(
                    getString(
                        R.string.notification_scanning_error,
                        state.error
                    ),
                    R.drawable.ic_scanning_24
                )
            }

            is BleState.Connecting -> NotificationData(
                getString(R.string.notification_connecting, state.device.identifier),
                R.drawable.ic_scanning_24
            )

            is BleState.Connected -> NotificationData(
                getString(R.string.notification_connected, state.bleDevice.name),
                R.drawable.ic_heart
            )

            is BleState.NotificationUpdate -> {
                with(state.bleNotification) {
                    if (isBleConnected.not()) {
                        NotificationData(
                            getString(R.string.notification_connection_lost),
                            R.drawable.ic_heart_disconnected
                        )
                    } else if (hrNotification?.isContactOn?.not() == true) {
                        NotificationData(getString(R.string.notification_no_contact), R.drawable.ic_heart_contact_off)
                    } else {
                        val hrBpm = hrNotification?.hrBpm ?: "--"
                        NotificationData(
                            getString(R.string.notification_current_hr, tracker.trackingState().text(), hrBpm),
                            R.drawable.ic_heart
                        )
                    }
                }
            }

            is BleState.Disconnected -> NotificationData(
                getString(R.string.notification_disconnected),
                R.drawable.ic_heart_disconnected
            )
        }

        updateNotification(notificationData)
    }

    private fun updateNotification(data: NotificationData) {
        val title = getString(R.string.notification_title)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setLargeIcon(Icon.createWithResource(this, data.iconRes))
            .setContentTitle(title)
            .setContentText(data.text)
            .setSilent(true)
            .setOngoing(true)
            .build()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    enum class Action {
        Scan,
        Connect,
        Disconnect,
        StartTracking,
        PauseTracking,
        StopTracking,
        CancelScanning
    }
}

data class NotificationData(
    val text: String,
    val iconRes: Int
)
