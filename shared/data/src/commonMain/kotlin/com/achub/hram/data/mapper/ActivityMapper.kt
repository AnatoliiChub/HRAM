package com.achub.hram.data.mapper

import com.achub.hram.data.db.entity.ActivityEntity
import com.achub.hram.models.ActivityRecord

fun ActivityEntity.toDomain() = ActivityRecord(
    id = id,
    name = name,
    duration = duration,
    startDate = startDate,
)

fun ActivityRecord.toEntity() = ActivityEntity(
    id = id,
    name = name,
    duration = duration,
    startDate = startDate,
)
