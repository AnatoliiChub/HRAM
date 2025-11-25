package com.achub.hram.data

import com.achub.hram.data.db.entity.ActivityEntity
import com.achub.hram.data.db.entity.ActivityGraphInfo
import com.achub.hram.data.db.entity.ActivityWithHeartRates
import com.achub.hram.data.db.entity.HeartRateEntity
import kotlinx.coroutines.flow.Flow

interface HrActivityRepo {

    suspend fun insert(item: HeartRateEntity)

    suspend fun insert(item: ActivityEntity)

    suspend fun updateNameById(id: String, name: String, duration: Long)

    suspend fun updateNameById(id: String, name: String)

    fun getActivityByName(name: String): ActivityEntity?

    fun getActivitiesGraph(): Flow<List<ActivityGraphInfo>>

    fun getActivityWithHeartRates(id: String): Flow<ActivityWithHeartRates>

    suspend fun deleteActivitiesById(ids: Set<String>)
}
