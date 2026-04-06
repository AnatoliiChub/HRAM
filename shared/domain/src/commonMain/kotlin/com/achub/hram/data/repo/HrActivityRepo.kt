package com.achub.hram.data.repo

import com.achub.hram.domain.model.ActivityInfo
import com.achub.hram.domain.model.ActivityRecord
import com.achub.hram.domain.model.HeartRateRecord
import kotlinx.coroutines.flow.Flow

interface HrActivityRepo {
    suspend fun insert(item: HeartRateRecord)

    suspend fun insert(item: ActivityRecord)

    suspend fun updateById(id: String, name: String, duration: Long)

    suspend fun updateById(id: String, name: String)

    suspend fun getActivity(id: String): ActivityRecord?

    fun getActivities(): Flow<List<ActivityInfo>>

    suspend fun getHeartRatesForActivity(activityId: String): List<HeartRateRecord>

    suspend fun deleteActivitiesById(ids: Set<String>)
}

