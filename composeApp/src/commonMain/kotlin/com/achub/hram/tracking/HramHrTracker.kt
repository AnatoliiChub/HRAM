package com.achub.hram.tracking

import com.achub.hram.ble.repo.BleConnectionRepo
import com.achub.hram.ble.repo.BleDataRepo
import com.achub.hram.data.model.BleDevice
import com.achub.hram.data.model.HrNotifications
import com.achub.hram.launchIn
import com.juul.kable.Advertisement
import com.juul.kable.Peripheral
import com.juul.kable.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
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
import org.koin.core.annotation.Single
import kotlin.time.Clock.System.now
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

const val SCAN_DURATION = 5_000L
private const val TAG = "HrTracker"

@Single
class HramHrTracker(val bleDataRepo: BleDataRepo, val bleConnectionRepo: BleConnectionRepo) : HrTracker {
    private val advertisements: MutableList<Advertisement> = mutableListOf()
    private var scanJobs = mutableListOf<Job>()
    private val listenJobs = mutableListOf<Job>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @OptIn(FlowPreview::class, ExperimentalUuidApi::class)
    override fun scan(onInit: () -> Unit, onUpdate: (List<BleDevice>) -> Unit, onComplete: () -> Unit) {
        cancelScanning()
        scope.launch(Dispatchers.Default) {
            val scannedDevices = mutableSetOf<BleDevice>()
            onInit()
            bleConnectionRepo.scanHrDevices()
                .onEach { advertisements.add(it) }
                .map { BleDevice(name = it.peripheralName ?: "", identifier = it.identifier.toString()) }
                .flowOn(Dispatchers.IO)
                .distinctUntilChanged()
                .onEach { device ->
                    scannedDevices.add(device)
                    onUpdate(scannedDevices.toList())
                }.onCompletion { onComplete() }
                .catch { _root_ide_package_.com.achub.hram.loggerE(TAG) { "Error: $it" } }
                .launchIn(scope = scope, context = Dispatchers.Default)
                .let { scanJobs.add(it) }
            delay(SCAN_DURATION)
            cancelScanning()
        }.let { scanJobs.add(it) }
    }

    @OptIn(ExperimentalCoroutinesApi::class, ExperimentalUuidApi::class)
    override fun listen(
        device: BleDevice,
        onInitConnection: () -> Unit,
        onConnected: (BleDevice) -> Unit,
        onNewIndications: (HrNotifications) -> Unit
    ) {
        cancelScanning()
        cancelConnection()
        advertisements.firstOrNull { it.identifier.toString() == device.identifier }?.let { advertisement ->
            onInitConnection()
            bleConnectionRepo.connectToDevice(advertisement.identifier)
                .withIndex()
                .onEach { (index, device) -> if (index == 0) onConnected(device) }
                .catch { _root_ide_package_.com.achub.hram.loggerE(TAG) { "Error while connecting to device: $it" } }
                .onCompletion { _root_ide_package_.com.achub.hram.logger(TAG) { "ConnectToDevice job completed" } }
                .launchIn(scope, Dispatchers.Default)
                .let { listenJobs.add(it) }
            bleConnectionRepo.onConnected
                .flatMapLatest { device -> hrIndicationCombiner(device) }
                .onEach { onNewIndications(it) }
                .catch { _root_ide_package_.com.achub.hram.loggerE(TAG) { "Error: $it" } }
                .launchIn(scope, Dispatchers.Default)
                .let { listenJobs.add(it) }
        }
    }

    override fun release() {
        cancelScanning()
        cancelConnection()
        scope.cancel()
    }

    @OptIn(ExperimentalTime::class)
    private fun hrIndicationCombiner(device: Peripheral): Flow<HrNotifications> = combine(
        bleDataRepo.observeHeartRate(device),
        bleDataRepo.observeBatteryLevel(device),
        device.state
    ) { hrRate, battery, state ->
        if (state !is State.Connected) {
            HrNotifications.Empty
        } else {
            HrNotifications(hrRate, battery, now().toEpochMilliseconds())
        }
    }

    override fun cancelScanning() {
        scanJobs.forEach { it.cancel() }
        scanJobs.clear()
    }

    private fun cancelConnection() {
        listenJobs.forEach { it.cancel() }
        listenJobs.clear()
    }
}