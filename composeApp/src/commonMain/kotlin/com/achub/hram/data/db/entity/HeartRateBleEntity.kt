package com.achub.hram.data.db.entity

import androidx.room.Entity

@Entity(primaryKeys = ["activityId", "elapsedTime"])
data class HeartRateBleEntity(
    val activityId: String,
    val heartRate: Int,
    val timestamp: Long,
    val elapsedTime: Long,
    val isContactOn: Boolean,
    val batteryLevel: Int,
)


