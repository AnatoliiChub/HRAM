package com.achub.hram.ext

import com.achub.hram.domain.model.BleNotificationModel
import com.achub.hram.domain.model.DeviceModel
import com.achub.hram.domain.model.HrNotificationModel
import com.achub.hram.models.DeviceUi
import com.achub.hram.models.BleNotificationUi
import com.achub.hram.models.HrNotificationUi

fun DeviceModel.toDto() = DeviceUi(name = name, identifier = identifier, manufacturer = manufacturer)

fun BleNotificationModel.toDto() = BleNotificationUi(
    hrNotification = hrNotification?.toDto(),
    batteryLevel = batteryLevel,
    isBleConnected = isBleConnected,
    elapsedTime = elapsedTime,
)

fun HrNotificationModel.toDto() = HrNotificationUi(
    hrBpm = hrBpm,
    isSensorContactSupported = isSensorContactSupported,
    isContactOn = isContactOn,
)
