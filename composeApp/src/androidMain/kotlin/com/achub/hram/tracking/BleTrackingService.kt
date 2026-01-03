package com.achub.hram.tracking

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.achub.hram.ble.models.BleDevice
import com.achub.hram.ble.models.HramBleDevice
import com.achub.hram.data.models.ScanError
import com.achub.hram.data.models.TrackingState
import com.achub.hram.data.repo.TrackingStateRepo
import com.achub.hram.di.CoroutineModule.Companion.WORKER_DISPATCHER
import com.achub.hram.ext.launchIn
import com.achub.hram.ext.logger
import com.achub.hram.ext.loggerE
import com.achub.hram.library.R
import com.juul.kable.UnmetRequirementException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinComponent
import org.koin.core.qualifier.named
import java.util.concurrent.atomic.AtomicInteger

const val CHANNEL_ID = "BLE_TRACKING_CHANNEL_ID"
const val CHANNEL_NAME = "Channel name"

const val ACTION = "com.achub.hram.tracking.BleTrackingService.ACTION"
const val NOTIFICATION_ID = 1
private const val TAG = "BleTrackingService"

class BleTrackingService : Service(), KoinComponent {
    companion object {
        const val EXTRA_DEVICE = "device"
    }

    private val trackingManager: ActivityTrackingManager by inject()
    private val trackingStateRepo: TrackingStateRepo by inject()

    private val dispatcher: CoroutineDispatcher by inject(qualifier = named(WORKER_DISPATCHER))
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private var hrTrackingJob: Job? = null
    private val currentAction = AtomicInteger(-1)

    override fun onCreate() {
        super.onCreate()
        val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(notificationChannel)
        val notification: Notification = notification("")
        ServiceCompat.startForeground(this, 1, notification, FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE)
        logger(TAG) { "Service created" }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logger(TAG) { "onStartCommand with action: $intent $dispatcher" }
        currentAction.set(Action.entries[intent?.getIntExtra(ACTION, -1) ?: -1].ordinal)
        val action = Action.entries[currentAction.get()]
        updateNotificationContent(action.name)

        when (action) {
            Action.Scan -> scan()
            Action.Connect -> intent?.getStringExtra(EXTRA_DEVICE)?.let { connect(identifier = it) }
            Action.Disconnect -> scope.launch { trackingStateRepo.updateTrackingState(TrackingState.Disconnected) }
        }
        return START_STICKY
    }

    private fun scan() {
        trackingManager.scan(
            onInit = { scope.launch { onInitScan() } },
            onUpdate = { devices -> scope.launch { onUpdateScan(devices) } },
            onComplete = { scope.launch { onScanComplete(it) } },
            onError = { scope.launch { onScanFailed(it) } }
        )
    }

    private fun connect(identifier: String) {
        val device = HramBleDevice(name = "", identifier = identifier)
        trackingManager.connect(
            device = device,
            onInitConnection = { scope.launch { onConnectionInit(device) } },
            onConnected = ::onConnected,
            onError = ::onConnectionFailed,
        )
    }

    private fun onConnected(bleDevice: BleDevice) = trackingManager.bleNotification
        .onStart { logger(TAG) { "Connected to $bleDevice" } }
        .onStart { trackingStateRepo.updateTrackingState(TrackingState.Connected(bleDevice)) }
        .onEach { trackingStateRepo.updateTrackingState(TrackingState.NotificationUpdate(it)) }
        .flowOn(dispatcher)
        .launchIn(scope)
        .let { hrTrackingJob = it }

    private fun onConnectionFailed(exception: Throwable? = null) = scope.launch {
        loggerE(TAG) { "Connection failed: $exception" }
        trackingManager.disconnect()
        trackingStateRepo.updateTrackingState(TrackingState.Disconnected)
    }

    private suspend fun onConnectionInit(device: HramBleDevice) {
        logger(TAG) { "Initializing connection to device: $device" }
        trackingStateRepo.updateTrackingState(TrackingState.Connecting(device))
    }

    private suspend fun onScanFailed(exception: Throwable) {
        loggerE(TAG) { "Scan failed: $exception" }
        val error = if (exception is UnmetRequirementException) ScanError.BLUETOOTH_OFF else null
        if (error == null) return
        val scanState = TrackingState.Scanning(devices = emptyList(), completed = false, error = error)
        trackingStateRepo.updateTrackingState(scanState)
    }

    private suspend fun onScanComplete(devices: List<BleDevice>) {
        logger(TAG) { "completeScan" }
        if (currentAction.get() != Action.Scan.ordinal) return
        trackingStateRepo.updateTrackingState(TrackingState.Scanning(devices, true, error = null))
    }

    private suspend fun onInitScan() {
        trackingStateRepo.updateTrackingState(TrackingState.Scanning(devices = emptyList()))
    }

    private suspend fun onUpdateScan(devices: List<BleDevice>) {
        if (currentAction.get() != Action.Scan.ordinal) return
        trackingStateRepo.updateTrackingState(TrackingState.Scanning(devices))
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        logger(TAG) { "Service destroyed" }
        scope.cancel()
    }

    private fun notification(content: String) = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("Ble Tracking Service")
        .setSmallIcon(R.drawable.ic_scanning_24)
        .setSilent(true)
        .setContentText(content).build()

    private fun updateNotificationContent(content: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification(content))
    }

    enum class Action {
        Scan,
        Connect,
        Disconnect,
    }
}
