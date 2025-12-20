package com.achub.hram.ble

import com.achub.hram.ble.models.BleDevice
import com.achub.hram.ble.models.BleNotification
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi

interface HrDeviceRepo {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun listen(): Flow<BleNotification>

    @OptIn(ExperimentalCoroutinesApi::class)
    fun connect(
        device: BleDevice,
        onInitConnection: () -> Unit,
        onConnected: (BleDevice) -> Unit,
        onError: (Throwable) -> Unit
    )

    fun cancelScanning()

    fun disconnect()

    @OptIn(FlowPreview::class, ExperimentalUuidApi::class)
    fun scan(
        onInit: () -> Unit,
        onUpdate: (List<BleDevice>) -> Unit,
        onComplete: () -> Unit,
        onError: (Throwable) -> Unit
    )
}
