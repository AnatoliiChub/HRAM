package com.achub.hram.ble

import com.achub.hram.ble.core.BleConnectionManager
import com.achub.hram.ble.core.BleDataRepo
import com.achub.hram.ble.model.BleDevice
import com.achub.hram.ble.model.BleNotification
import com.achub.hram.ext.cancelAndClear
import com.achub.hram.ext.launchIn
import com.achub.hram.ext.logger
import com.achub.hram.ext.loggerE
import com.juul.kable.Advertisement
import com.juul.kable.Peripheral
import com.juul.kable.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

const val SCAN_DURATION = 5_000L
private const val TAG = "HramHrDeviceRepo"

class HramHrDeviceRepo(
    @InjectedParam val scope: CoroutineScope,
    val bleDataRepo: BleDataRepo,
    val bleConnectionManager: BleConnectionManager
) : HrDeviceRepo {
    private val advertisements: MutableList<Advertisement> = mutableListOf()
    private var scanJobs = mutableListOf<Job>()
    private val connectionJobs = mutableListOf<Job>()

    @OptIn(FlowPreview::class, ExperimentalUuidApi::class)
    override fun scan(onInit: () -> Unit, onUpdate: (List<BleDevice>) -> Unit, onComplete: () -> Unit) {
        cancelScanning()
        scope.launch(Dispatchers.Default) {
            val scannedDevices = mutableSetOf<BleDevice>()
            onInit()
            bleConnectionManager.scanHrDevices()
                .onEach { advertisements.add(it) }
                .map { BleDevice(name = it.peripheralName ?: "", identifier = it.identifier.toString()) }
                .flowOn(Dispatchers.IO)
                .distinctUntilChanged()
                .onEach { device ->
                    scannedDevices.add(device)
                    onUpdate(scannedDevices.toList())
                }.onCompletion { onComplete() }
                .catch { loggerE(TAG) { "Error: $it" } }
                .flowOn(Dispatchers.Default)
                .launchIn(scope = scope)
                .let { scanJobs.add(it) }
            delay(SCAN_DURATION)
            cancelScanning()
        }.let { scanJobs.add(it) }
    }

    @OptIn(ExperimentalUuidApi::class)
    override fun connect(
        device: BleDevice,
        onInitConnection: () -> Unit,
        onConnected: (BleDevice) -> Unit
    ) {
        cancelScanning()
        cancelConnection()
        advertisements.firstOrNull { it.identifier.toString() == device.identifier }?.let { advertisement ->
            onInitConnection()
            bleConnectionManager.connectToDevice(advertisement.identifier)
                .withIndex()
                .onEach { (index, device) -> if (index == 0) onConnected(device) }
                .catch { loggerE(TAG) { "Error while connecting to device: $it" } }
                .onCompletion { logger(TAG) { "ConnectToDevice job completed" } }
                .flowOn(Dispatchers.Default)
                .launchIn(scope)
                .let { connectionJobs.add(it) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun listen() = bleConnectionManager.onConnected
        .flatMapLatest { device -> hrIndicationCombiner(device) }
        .catch { loggerE(TAG) { "Error: $it" } }

    override fun disconnect() {
        scope.launch {
            bleConnectionManager.disconnect()
            cancelScanning()
            cancelConnection()
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun hrIndicationCombiner(device: Peripheral): Flow<BleNotification> = combine(
        bleDataRepo.observeHeartRate(device),
        bleDataRepo.observeBatteryLevel(device),
        device.state
    ) { hrNotification, battery, state -> BleNotification(hrNotification, battery, state is State.Connected) }

    override fun cancelScanning() = scanJobs.cancelAndClear()

    private fun cancelConnection() = connectionJobs.cancelAndClear()
}
