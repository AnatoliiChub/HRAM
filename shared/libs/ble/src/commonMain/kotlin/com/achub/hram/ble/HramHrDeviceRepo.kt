package com.achub.hram.ble

import com.achub.hram.Logger
import com.achub.hram.ble.core.connection.BleConnectionManager
import com.achub.hram.ble.core.data.BleDataRepo
import com.achub.hram.ble.models.BleConnectionsException.BleUnavailableException
import com.achub.hram.ble.models.BleDevice
import com.achub.hram.ble.models.BleNotification
import com.achub.hram.ble.models.HramBleDevice
import com.achub.hram.ble.utils.cancelAfter
import com.juul.kable.Peripheral
import com.juul.kable.State
import com.juul.kable.UnmetRequirementException
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
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

private const val TAG = "HramHrDeviceRepo"

internal class HramHrDeviceRepo(
    val bleDataRepo: BleDataRepo,
    val bleConnectionManager: BleConnectionManager
) : HrDeviceRepo {
    @OptIn(FlowPreview::class, ExperimentalUuidApi::class)
    override fun scan(duration: Duration): Flow<ScanResult> =
        bleConnectionManager.scanHrDevices()
            .cancelAfter(duration)
            .map { HramBleDevice(name = it.peripheralName ?: "", identifier = it.identifier.toString()) }
            .distinctUntilChanged()
            .map { device -> ScanResult.ScanUpdate(device) as ScanResult }
            .catch {
                when (it) {
                    is kotlinx.coroutines.TimeoutCancellationException -> {
                        Logger.d(TAG) { "Scan completed after timeout of $duration" }
                        emit(ScanResult.Complete)
                    }

                    is UnmetRequirementException -> {
                        Logger.e(TAG) { "BLE unavailable during scan: $it" }
                        emit(ScanResult.Error(BleUnavailableException(cause = it)))
                    }

                    else -> {
                        Logger.e(TAG) { "Error while scanning for devices: $it" }
                        emit(ScanResult.Error(it))
                    }
                }
            }

    @OptIn(ExperimentalUuidApi::class)
    override fun connect(device: BleDevice): Flow<ConnectionResult> =
        bleConnectionManager.connectToDevice(device.provideIdentifier())
            .withIndex()
            .filter { it.index == 0 }
            .map { ConnectionResult.Connected(it.value) as ConnectionResult }
            .catch { error ->
                Logger.e(TAG) { "Error while connecting to device: $error" }
                emit(ConnectionResult.Error(error))
            }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun listen(): Flow<BleNotification> = bleConnectionManager.onConnected
        .flatMapLatest { device -> hrIndicationCombiner(device) }
        .catch { Logger.e(TAG) { "Error: $it" } }

    override suspend fun disconnect() {
        bleConnectionManager.disconnect()
    }

    @OptIn(ExperimentalTime::class)
    private fun hrIndicationCombiner(device: Peripheral): Flow<BleNotification> = combine(
        bleDataRepo.observeHeartRate(device),
        bleDataRepo.observeBatteryLevel(device),
        device.state.onEach { Logger.d(TAG) { "Device state changed. $it " } }
    ) { hr, battery, state ->
        val isConnected = state is State.Connected
        if (isConnected) BleNotification(hr, battery, true) else BleNotification.Empty
    }
}
