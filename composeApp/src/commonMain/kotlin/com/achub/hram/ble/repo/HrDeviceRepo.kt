package com.achub.hram.ble.repo

import com.achub.hram.data.model.BleDevice
import com.achub.hram.data.model.HrIndication
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlin.uuid.ExperimentalUuidApi

interface HrDeviceRepo {

    val latestIndications: Channel<HrIndication>
    @OptIn(FlowPreview::class, ExperimentalUuidApi::class)
    fun scan(onInit: () -> Unit, onUpdate: (List<BleDevice>) -> Unit, onComplete: () -> Unit)
    @OptIn(ExperimentalCoroutinesApi::class)
    fun connect(
        device: BleDevice,
        onInitConnection: () -> Unit,
        onConnected: (BleDevice) -> Unit,
    )

    fun cancelScanning()
    fun release()
}