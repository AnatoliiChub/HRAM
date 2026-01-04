package com.achub.hram.ble

import com.achub.hram.ble.core.connection.BleConnectionManager
import com.achub.hram.ble.core.data.BleDataRepo
import com.achub.hram.ble.models.BleDevice
import com.achub.hram.ble.models.BleNotification
import com.achub.hram.ble.models.HramBleDevice
import com.achub.hram.di.WorkerThread
import com.achub.hram.ext.cancelAfter
import com.achub.hram.ext.logger
import com.achub.hram.ext.loggerE
import com.juul.kable.Peripheral
import com.juul.kable.State
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.withIndex
import org.koin.core.component.KoinComponent
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

private const val TAG = "HramHrDeviceRepo"

class HramHrDeviceRepo(
    val bleDataRepo: BleDataRepo,
    @param:WorkerThread
    val dispatcher: CoroutineDispatcher,
    val bleConnectionManager: BleConnectionManager
) : HrDeviceRepo, KoinComponent {
    @OptIn(FlowPreview::class, ExperimentalUuidApi::class)
    override fun scan(duration: Duration): Flow<ScanResult> =
        bleConnectionManager.scanHrDevices()
            .cancelAfter(duration)
            .map { HramBleDevice(name = it.peripheralName ?: "", identifier = it.identifier.toString()) }
            .distinctUntilChanged()
            .map { device -> ScanResult.ScanUpdate(device) as ScanResult }
            .catch {
                if (it is kotlinx.coroutines.TimeoutCancellationException) {
                    logger(TAG) { "Scan completed after timeout of $duration" }
                    emit(ScanResult.Complete)
                } else {
                    loggerE(TAG) { "Error while scanning for devices: $it" }
                    emit(ScanResult.Error(it))
                }
            }

    @OptIn(ExperimentalUuidApi::class)
    override fun connect(device: BleDevice): Flow<ConnectionResult> =
        bleConnectionManager.connectToDevice(device.provideIdentifier())
            .withIndex()
            .filter { it.index == 0 }
            .map { ConnectionResult.Connected(it.value) as ConnectionResult }
            .catch { error ->
                loggerE(TAG) { "Error while connecting to device: $error" }
                emit(ConnectionResult.Error(error))
            }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun listen(): Flow<BleNotification> = bleConnectionManager.onConnected
        .flatMapLatest { device -> hrIndicationCombiner(device) }
        .catch { loggerE(TAG) { "Error: $it" } }

    override suspend fun disconnect() {
        bleConnectionManager.disconnect()
    }

    @OptIn(ExperimentalTime::class)
    private fun hrIndicationCombiner(device: Peripheral): Flow<BleNotification> = combine(
        bleDataRepo.observeHeartRate(device),
        bleDataRepo.observeBatteryLevel(device),
        device.state.onEach { logger(TAG) { "Device state changed. $it " } }
    ) { hr, battery, state ->
        val isConnected = state is State.Connected
        if (isConnected) BleNotification(hr, battery, true) else BleNotification.Empty
    }
}
