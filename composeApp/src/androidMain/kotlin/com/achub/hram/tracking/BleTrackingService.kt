package com.achub.hram.tracking

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
import android.os.IBinder
import androidx.core.app.ServiceCompat
import com.achub.hram.BLE_SCAN_DURATION
import com.achub.hram.ble.models.HramBleDevice
import com.achub.hram.data.repo.state.TrackingStateRepo
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.sample
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
    private val trackerRepo: TrackingStateRepo by inject()
    private val dispatcher: CoroutineDispatcher by inject(qualifier = named(WORKER_DISPATCHER))
    private val notificator: Notificator by inject()
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private val currentAction = AtomicInteger(-1)
    private var scanJob: Job? = null
    private var connectJob: Job? = null
    private var trackingJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        val channel = getString(R.string.channel_name)
        val notificationChannel = NotificationChannel(CHANNEL_ID, channel, NotificationManager.IMPORTANCE_DEFAULT)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(notificationChannel)
        val notification: Notification = notificator.createNotification()
        ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE)
        trackBleState()
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

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        logger(TAG) { "Service destroyed" }
        job.cancel()
        scanJob = null
        connectJob = null
        trackingJob = null
        scope.cancel()
    }

    @OptIn(FlowPreview::class)
    private fun trackBleState() {
        trackingJob?.cancel()
        tracker.observeBleState()
            .combine(
                trackerRepo.listen().onStart { TrackingStateStage.TRACKING_INIT_STATE }
            ) { bleState, trackingState ->
                Pair(bleState, trackingState)
            }.sample(NOTIFICATION_SAMPLE_MS)
            .distinctUntilChanged()
            .onEach { notificator.updateNotification(it.first, it.second) }
            .flowOn(dispatcher)
            .launchIn(scope)
            .let { trackingJob = it }
    }

    private fun stopTracking(intent: Intent) =
        scope.launch { tracker.finishTracking(intent.getStringExtra(EXTRA_ACTIVITY_NAME)) }

    private fun connect(intent: Intent) {
        connectJob?.cancel()
        intent.getStringExtra(EXTRA_DEVICE_ID)?.let { identifier ->
            val device = HramBleDevice(name = intent.getStringExtra(EXTRA_DEVICE_NAME) ?: "", identifier = identifier)
            tracker.connectAndSubscribe(device = device)
                .launchIn(scope)
                .let { connectJob = it }
        }
    }

    @OptIn(FlowPreview::class)
    private fun scan() {
        scanJob?.cancel()
        tracker.scan(BLE_SCAN_DURATION.milliseconds)
            .filter { currentAction.get() == Action.Scan.ordinal }
            .flowOn(dispatcher)
            .launchIn(scope)
            .let { scanJob = it }
    }
}
