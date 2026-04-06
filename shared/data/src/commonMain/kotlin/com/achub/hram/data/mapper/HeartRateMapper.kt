package com.achub.hram.data.mapper

import com.achub.hram.data.db.entity.HeartRateBleEntity
import com.achub.hram.domain.model.HeartRateRecord

fun HeartRateBleEntity.toDomain() = HeartRateRecord(
    activityId = activityId,
    heartRate = heartRate,
    timestamp = timestamp,
    elapsedTime = elapsedTime,
    isContactOn = isContactOn,
    batteryLevel = batteryLevel,
)

fun HeartRateRecord.toEntity() = HeartRateBleEntity(
    activityId = activityId,
    heartRate = heartRate,
    timestamp = timestamp,
    elapsedTime = elapsedTime,
    isContactOn = isContactOn,
    batteryLevel = batteryLevel,
)

