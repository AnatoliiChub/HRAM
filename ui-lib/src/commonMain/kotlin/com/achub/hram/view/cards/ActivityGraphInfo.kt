package com.achub.hram.view.cards

import com.achub.hram.models.GraphLimitsUi

data class ActivityGraphInfo(
    val name: String,
    val id: String,
    val startDate: Long,
    val duration: Long,
    val buckets: List<AvgHrBucketByActivity>,
    val totalRecords: Int,
    val avgHr: Int,
    val maxHr: Int,
    val minHr: Int,
    val limits: GraphLimitsUi,
)
