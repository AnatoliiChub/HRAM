package com.achub.hram.model

data class ActivityInfo(
    val id: String,
    val name: String,
    val startDate: Long,
    val duration: Long,
    val buckets: List<HrBucket>,
    val totalRecords: Int,
    val avgHr: Int,
    val maxHr: Int,
    val minHr: Int,
)

