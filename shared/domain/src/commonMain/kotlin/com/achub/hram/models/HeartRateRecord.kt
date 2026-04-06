package com.achub.hram.models

data class HeartRateRecord(
    val activityId: String,
    val heartRate: Int,
    val timestamp: Long,
    val elapsedTime: Long,
    val isContactOn: Boolean,
    val batteryLevel: Int,
)
