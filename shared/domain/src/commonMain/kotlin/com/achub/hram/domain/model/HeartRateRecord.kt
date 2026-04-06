package com.achub.hram.domain.model

data class HeartRateRecord(
    val activityId: String,
    val heartRate: Int,
    val timestamp: Long,
    val elapsedTime: Long,
    val isContactOn: Boolean,
    val batteryLevel: Int,
)

