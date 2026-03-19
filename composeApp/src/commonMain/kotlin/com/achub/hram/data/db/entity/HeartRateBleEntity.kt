package com.achub.hram.data.db.entity

import androidx.room.Entity
import com.achub.hram.data.models.GraphLimits

@Entity(primaryKeys = ["activityId", "elapsedTime"])
data class HeartRateBleEntity(
    val activityId: String,
    val heartRate: Int,
    val timestamp: Long,
    val elapsedTime: Long,
    val isContactOn: Boolean,
    val batteryLevel: Int,
)

data class AvgHrBucketByActivity(
    val bucketNumber: Int,
    val avgHr: Float,
    val elapsedTime: Long,
)

data class ActivityGraphInfo(
    val activity: ActivityEntity,
    val buckets: List<AvgHrBucketByActivity>,
    val totalRecords: Int,
    val avgHr: Int,
    val maxHr: Int,
    val minHr: Int,
    val limits: GraphLimits,
)
