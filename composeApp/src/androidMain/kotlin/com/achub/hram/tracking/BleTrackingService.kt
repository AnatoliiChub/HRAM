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
import com.achub.hram.ble.ConnectionResult
import com.achub.hram.ble.ScanResult
import com.achub.hram.ble.models.BleDevice
import com.achub.hram.ble.models.HramBleDevice
import com.achub.hram.data.models.BleState
import com.achub.hram.data.models.ScanError
import com.achub.hram.data.repo.state.BleStateRepo
import com.achub.hram.data.repo.state.TrackingStateRepo
import com.achub.hram.di.CoroutineModule.Companion.WORKER_DISPATCHER
import com.achub.hram.ext.launchIn
import com.achub.hram.ext.logger
import com.achub.hram.ext.loggerE
import com.achub.hram.library.R
import com.juul.kable.UnmetRequirementException
import dev.icerock.moko.permissions.DeniedException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
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

private const val SCAN_DEBOUNCE_MS = 500L

class BleTrackingService : Service(), KoinComponent {
    companion object {
        const val EXTRA_DEVICE = "device"
        const val EXTRA_NAME = "name"
    }

    private val trackingManager: ActivityTrackingManager by inject()
    private val bleStateRepo: BleStateRepo by inject()
    private val trackingStateRepo: TrackingStateRepo by inject()

    private val dispatcher: CoroutineDispatcher by inject(qualifier = named(WORKER_DISPATCHER))
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private var hrTrackingJob: Job? = null
    private val currentAction = AtomicInteger(-1)

    private var scanJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        val notificationChannel =
            NotificationChannel(CHANNEL_ID, getString(R.string.channel_name), NotificationManager.IMPORTANCE_DEFAULT)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(notificationChannel)
        val notification: Notification = createNotification()
        ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE)
        scope.launch { trackingStateRepo.release() }
        logger(TAG) { "Service created" }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logger(TAG) { "onStartCommand with action: $intent $dispatcher" }
        if (intent == null) return START_STICKY
        currentAction.set(Action.entries[intent.getIntExtra(ACTION, -1)].ordinal)
        val action = Action.entries[currentAction.get()]

        when (action) {
            Action.Scan -> scan()

            Action.Connect -> {
                scanJob?.cancel()
                scanJob = null
                intent.getStringExtra(EXTRA_DEVICE)?.let { connect(identifier = it) }
            }

            Action.Disconnect -> {
                hrTrackingJob?.cancel()
                hrTrackingJob = null
                scope.launch { updateState(BleState.Disconnected) }
            }

            Action.StartTracking -> scope.launch { trackingManager.startTracking() }

            Action.PauseTracking -> scope.launch { trackingManager.pauseTracking() }

            Action.StopTracking -> {
                val name = intent.getStringExtra(EXTRA_NAME)
                scope.launch { trackingManager.finishTracking(name) }
            }
        }
        return START_STICKY
    }

    @OptIn(FlowPreview::class)
    private fun scan() {
        trackingManager.scan(BLE_SCAN_DURATION.milliseconds)
            .filter { currentAction.get() == Action.Scan.ordinal }
            .onStart { emit(ScanResult.Initiated) }
            .flowOn(dispatcher)
            .debounce { SCAN_DEBOUNCE_MS.milliseconds }
            .onEach {
                when (it) {
                    is ScanResult.Initiated -> onInitScan()
                    is ScanResult.Complete -> onScanComplete()
                    is ScanResult.Error -> onScanFailed(it.error)
                    is ScanResult.ScanUpdate -> onUpdateScan(it.device)
                }
            }
            .launchIn(scope)
            .let { scanJob = it }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun connect(identifier: String) {
        val device = HramBleDevice(name = "", identifier = identifier)
        trackingManager.connect(device = device)
            .onStart { onConnectionInit(device) }
            .onEach { if (it is ConnectionResult.Error) onConnectionFailed(it.error) }
            .filter { result -> result is ConnectionResult.Connected }
            .map { it as ConnectionResult.Connected }
            .map { it.device }
            .onEach { updateState(BleState.Connected(it)) }
            .flatMapLatest { device ->
                trackingManager.listen()
                    .onEach { notification ->
                        logger(TAG) { "Received notification: $notification from device: $device" }
                        val state = BleState.NotificationUpdate(notification, device)
                        updateState(state)
                    }
            }
            .launchIn(scope)
            .let { hrTrackingJob = it }
    }

    fun onConnectionFailed(exception: Throwable? = null) = scope.launch {
        loggerE(TAG) { "Connection failed: $exception" }
        trackingManager.disconnect()
        updateState(BleState.Disconnected)
    }

    private suspend fun onConnectionInit(device: HramBleDevice) {
        logger(TAG) { "Initializing connection to device: $device" }
        updateState(BleState.Connecting(device))
    }

    private suspend fun onScanFailed(exception: Throwable) {
        loggerE(TAG) { "Scan failed: $exception" }
        val error = when (exception) {
            is DeniedException if exception.message == "Bluetooth is powered off" -> ScanError.BLUETOOTH_OFF
            is UnmetRequirementException -> ScanError.BLUETOOTH_OFF
            else -> ScanError.NO_BLE_PERMISSIONS
        }
        updateState(BleState.Scanning.Error(error, System.currentTimeMillis()))
    }

    private suspend fun onScanComplete() {
        logger(TAG) { "completeScan" }
        if (currentAction.get() != Action.Scan.ordinal) return
        updateState(BleState.Scanning.Completed)
    }

    private suspend fun onInitScan() {
        updateState(BleState.Scanning.Started)
    }

    private suspend fun onUpdateScan(device: BleDevice) {
        if (currentAction.get() != Action.Scan.ordinal) return
        updateState(BleState.Scanning.Update(device))
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        logger(TAG) { "Service destroyed" }
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
                            getString(R.string.notification_current_hr, trackingStateRepo.get().text(), hrBpm),
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

    private suspend fun updateState(state: BleState) {
        bleStateRepo.update(state)
        updateNotification(state)
    }

    enum class Action {
        Scan,
        Connect,
        Disconnect,
        StartTracking,
        PauseTracking,
        StopTracking,
    }
}

fun TrackingStateStage.text() = when (this) {
    TrackingStateStage.TRACKING_INIT_STATE -> ""
    TrackingStateStage.ACTIVE_TRACKING_STATE -> "Tracking, "
    TrackingStateStage.PAUSED_TRACKING_STATE -> "Paused, "
}

data class NotificationData(
    val text: String,
    val iconRes: Int
)
