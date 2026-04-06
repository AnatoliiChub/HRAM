package com.achub.hram.data.mapper

import com.achub.hram.ble.ConnectionResult
import com.achub.hram.ble.ScanResult
import com.achub.hram.ble.models.BleDevice
import com.achub.hram.ble.models.BleNotification
import com.achub.hram.ble.models.HramBleDevice
import com.achub.hram.ble.models.HrNotification
import com.achub.hram.domain.model.BleNotificationModel
import com.achub.hram.domain.model.ConnectionResultModel
import com.achub.hram.domain.model.DeviceModel
import com.achub.hram.domain.model.HrNotificationModel
import com.achub.hram.domain.model.ScanResultModel

// ── BleDevice ↔ DeviceModel ──────────────────────────────────────────────────

fun BleDevice.toDomain() = DeviceModel(
    name = name,
    identifier = identifier,
    manufacturer = manufacturer,
)

fun DeviceModel.toBle(): BleDevice = HramBleDevice(
    name = name,
    identifier = identifier,
    manufacturer = manufacturer,
)

// ── HrNotification ↔ HrNotificationModel ────────────────────────────────────

fun HrNotification.toDomain() = HrNotificationModel(
    hrBpm = hrBpm,
    isSensorContactSupported = isSensorContactSupported,
    isContactOn = isContactOn,
)

// ── BleNotification ↔ BleNotificationModel ──────────────────────────────────

fun BleNotification.toDomain() = BleNotificationModel(
    hrNotification = hrNotification?.toDomain(),
    batteryLevel = batteryLevel,
    isBleConnected = isBleConnected,
    elapsedTime = elapsedTime,
)

// ── ScanResult ↔ ScanResultModel ────────────────────────────────────────────

fun ScanResult.toDomain(): ScanResultModel = when (this) {
    is ScanResult.ScanUpdate -> ScanResultModel.ScanUpdate(device.toDomain())
    is ScanResult.Error -> ScanResultModel.Error(error)
    ScanResult.Complete -> ScanResultModel.Complete
    ScanResult.Initiated -> ScanResultModel.Initiated
}

// ── ConnectionResult ↔ ConnectionResultModel ─────────────────────────────────

fun ConnectionResult.toDomain(): ConnectionResultModel = when (this) {
    is ConnectionResult.Connected -> ConnectionResultModel.Connected(device.toDomain())
    is ConnectionResult.Error -> ConnectionResultModel.Error(error)
}

