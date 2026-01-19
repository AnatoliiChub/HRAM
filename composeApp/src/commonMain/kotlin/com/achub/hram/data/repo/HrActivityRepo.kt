package com.achub.hram.data.repo

import com.achub.hram.data.db.entity.ActivityEntity
import com.achub.hram.data.db.entity.ActivityGraphInfo
import com.achub.hram.data.db.entity.ActivityWithHeartRates
import com.achub.hram.data.db.entity.HeartRateEntity
import kotlinx.coroutines.flow.Flow

interface HrActivityRepo {
    suspend fun insert(item: HeartRateEntity)

    suspend fun insert(item: ActivityEntity)

    suspend fun updateById(id: String, name: String, duration: Long)

    suspend fun updateById(id: String, name: String)

    suspend fun getActivity(id: String): ActivityEntity?

    fun getActivitiesGraph(): Flow<List<ActivityGraphInfo>>

    fun getActivityWithHeartRates(id: String): Flow<ActivityWithHeartRates>

    suspend fun getHeartRatesForActivity(activityId: String): List<HeartRateEntity>

    suspend fun deleteActivitiesById(ids: Set<String>)
}
