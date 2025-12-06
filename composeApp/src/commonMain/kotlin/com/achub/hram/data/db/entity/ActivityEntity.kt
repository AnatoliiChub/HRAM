package com.achub.hram.data.db.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

const val ACTIVE_ACTIVITY = ""

@Entity
data class ActivityEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val duration: Long,
    val startDate: Long,
)

data class ActivityWithHeartRates(
    @Embedded val activity: ActivityEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "activityId"
    )
    val heartRates: List<HeartRateEntity>
)
