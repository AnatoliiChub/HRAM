package com.achub.hram.tracking

import com.achub.hram.data.model.BleDevice
import com.achub.hram.data.model.HrNotifications
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlin.uuid.ExperimentalUuidApi

interface HrTracker {
    @OptIn(FlowPreview::class, ExperimentalUuidApi::class)
    fun scan(onInit: () -> Unit, onUpdate: (List<BleDevice>) -> Unit, onComplete: () -> Unit)
    @OptIn(ExperimentalCoroutinesApi::class)
    fun listen(
        device: BleDevice,
        onInitConnection: () -> Unit,
        onConnected: (BleDevice) -> Unit,
        onNewIndications: (HrNotifications) -> Unit
    )

    fun cancelScanning()
    fun release()
}