package com.achub.hram.data.db.entity

/** Internal Room query projection — mapped to domain HrBucket in the repo layer. */
data class HrBucketEntity(
    val bucketNumber: Int,
    val avgHr: Float,
    val elapsedTime: Long,
)

