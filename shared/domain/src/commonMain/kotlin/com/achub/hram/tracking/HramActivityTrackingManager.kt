package com.achub.hram.tracking

import com.achub.hram.Logger
import com.achub.hram.di.WorkerThread
import com.achub.hram.ext.tickerFlow
import com.achub.hram.models.BleNotificationModel
import com.achub.hram.models.DeviceModel
import com.achub.hram.models.ScanResultModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

private const val TAG = "HramActivityTrackingManager"

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class HramActivityTrackingManager(
    @param:WorkerThread
    private val dispatcher: CoroutineDispatcher,
    private val bleOrchestrator: BleConnectionOrchestrator,
    private val sessionRecorder: SessionRecorder,
) : ActivityTrackingManager {
    private val scope = CoroutineScope(dispatcher + SupervisorJob())

    override fun startTracking() = sessionRecorder.startTracking()

    override fun pauseTracking() = sessionRecorder.pauseTracking()

    override fun finishTracking(name: String?) = sessionRecorder.finishTracking(name)

    override fun releaseState() = sessionRecorder.releaseState()

    override suspend fun trackingState() = sessionRecorder.trackingState()

    override fun cancelScanning() = bleOrchestrator.cancelScanning()

    override fun observeBleState() = bleOrchestrator.observeBleState()

    override fun scan(duration: Duration): Flow<ScanResultModel> = bleOrchestrator.scan(duration)

    override fun disconnect() {
        scope.launch(dispatcher) {
            bleOrchestrator.disconnect()
            sessionRecorder.cancelJobs()
        }
    }

    override fun connectAndSubscribe(device: DeviceModel): Flow<BleNotificationModel> =
        bleOrchestrator.connectAndSubscribe(device)
            .combine(
                tickerFlow(1.seconds).filter { sessionRecorder.isTracking() }.onStart { emit(Unit) }
            ) { notification, _ -> notification }
            .onStart { emit(BleNotificationModel.Empty) }
            .map { it.copy(elapsedTime = sessionRecorder.elapsedTime()) }
            .onEach { indication ->
                if (sessionRecorder.isTracking() && indication.isBleConnected) sessionRecorder.record(indication)
            }
            .catch { Logger.e(TAG) { "listen error: $it" } }
            .onEach { notification ->
                Logger.d(TAG) { "Ble notification received: $notification" }
                bleOrchestrator.reportNotification(notification, device)
            }
}

class ScanCancelledException : CancellationException("Scan cancelled by user")
