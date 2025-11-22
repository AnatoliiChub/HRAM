package com.achub.hram.data.db.entity

import androidx.room.Entity
import com.achub.hram.data.models.GraphLimits

@Entity(primaryKeys = ["activityId", "timeStamp"])
data class HeartRateEntity(
    var activityId: String,
    var heartRate: Int,
    var timeStamp: Long,
)

data class AvgHrBucketByActivity(
    val bucketNumber: Int,
    val avgHr: Float,
    val timestamp: Long,
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
