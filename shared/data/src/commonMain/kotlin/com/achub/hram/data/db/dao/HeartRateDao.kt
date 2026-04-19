package com.achub.hram.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import com.achub.hram.data.db.entity.HeartRateBleEntity
import com.achub.hram.data.db.entity.HrBucketEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HeartRateDao {
    @Insert(onConflict = REPLACE)
    suspend fun insert(item: HeartRateBleEntity)

    @Insert(onConflict = REPLACE)
    suspend fun insertAll(items: List<HeartRateBleEntity>)

    @Query("SELECT * FROM HeartRateBleEntity")
    fun getAll(): Flow<List<HeartRateBleEntity>>

    @Query("SELECT * FROM HeartRateBleEntity WHERE activityId = :activityId ORDER BY elapsedTime ASC")
    suspend fun getAllForActivity(activityId: String): List<HeartRateBleEntity>

    @Query(
        """
    SELECT 
        hr.activityId,
        CAST((hr.elapsedTime * 100.0) / (act.duration) AS INT) AS bucketNumber,
        AVG(hr.heartRate) AS avgHr,
        MIN(hr.elapsedTime) AS elapsedTime
    FROM HeartRateBleEntity hr
    JOIN ActivityEntity act ON hr.activityId = act.id
    WHERE act.duration > 0
    GROUP BY hr.activityId, bucketNumber
    ORDER BY hr.activityId, bucketNumber
"""
    )
    suspend fun getAllAggregatedHeartRates(): List<HrBucketEntity>

    @Query("SELECT activityId, COUNT(*) as totalRecords FROM HeartRateBleEntity GROUP BY activityId")
    suspend fun getActivityCounts(): List<ActivityCount>

    @Query("DELETE from HeartRateBleEntity where activityId in (:ids)")
    suspend fun deleteRecordsByIds(ids: Set<String>)
}

data class ActivityCount(
    val activityId: String,
    val totalRecords: Int,
)
