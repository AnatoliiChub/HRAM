package com.achub.hram.ext

import com.achub.hram.ble.models.BleDevice
import com.achub.hram.ble.models.BleNotification
import com.achub.hram.ble.models.HrNotification
import com.achub.hram.models.DeviceUi
import com.achub.hram.models.BleNotificationUi
import com.achub.hram.models.HrNotificationUi

fun BleDevice.toDto() = DeviceUi(name = name, identifier = identifier, manufacturer = manufacturer)

fun BleNotification.toDto() = BleNotificationUi(
    hrNotification = hrNotification?.toDto(),
    batteryLevel = batteryLevel,
    isBleConnected = isBleConnected,
    elapsedTime = elapsedTime,
)

fun HrNotification.toDto() = HrNotificationUi(
    hrBpm = hrBpm,
    isSensorContactSupported = isSensorContactSupported,
    isContactOn = isContactOn,
)
