package com.achub.hram.data.db.entity

import androidx.room.Entity
import com.achub.hram.data.models.GraphLimits

@Entity(primaryKeys = ["activityId", "elapsedTime"])
data class HeartRateEntity(
    var activityId: String,
    var heartRate: Int,
    var elapsedTime: Long,
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
