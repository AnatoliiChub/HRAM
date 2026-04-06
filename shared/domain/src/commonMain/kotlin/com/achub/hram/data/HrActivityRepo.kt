package com.achub.hram.data

import com.achub.hram.models.ActivityInfo
import com.achub.hram.models.ActivityRecord
import com.achub.hram.models.HeartRateRecord
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
