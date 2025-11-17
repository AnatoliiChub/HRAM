package com.achub.hram.data.db.entity

import androidx.room.Entity

@Entity(primaryKeys = ["activityId", "timeStamp"])
data class HeartRateEntity(
    var activityId: String,
    var heartRate: Int,
    var timeStamp: Long,
)

data class AvgHrBucketByActivity(
    val bucketNumber: Int,
    val avgHr: Float
)

data class ActivityGraphInfo(
    val activity: ActivityEntity,
    val buckets: List<AvgHrBucketByActivity>,
    val totalRecords: Int,
)
