package com.achub.hram.ble.repo

import com.achub.hram.data.models.BleDevice
import com.achub.hram.data.models.BleIndication
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi

interface HrDeviceRepo {

    @OptIn(FlowPreview::class, ExperimentalUuidApi::class)
    fun scan(onInit: () -> Unit, onUpdate: (List<BleDevice>) -> Unit, onComplete: () -> Unit)
    @OptIn(ExperimentalCoroutinesApi::class)
    fun listen(): Flow<BleIndication>
    fun cancelScanning()
    fun disconnect()
    @OptIn(ExperimentalCoroutinesApi::class)
    fun connect(
        device: BleDevice,
        onInitConnection: () -> Unit,
        onConnected: (BleDevice) -> Unit,
    )
}
