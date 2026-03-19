package com.achub.hram.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import com.achub.hram.data.db.entity.AvgHrBucketByActivity
import com.achub.hram.data.db.entity.HeartRateBleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HeartRateDao {
    @Insert(onConflict = REPLACE)
    suspend fun insert(item: HeartRateBleEntity)

    @Query("SELECT * FROM HeartRateBleEntity")
    fun getAll(): Flow<List<HeartRateBleEntity>>

    @Query("SELECT * FROM HeartRateBleEntity WHERE activityId = :activityId ORDER BY elapsedTime ASC")
    suspend fun getAllForActivity(activityId: String): List<HeartRateBleEntity>

    @Query(
        """
    SELECT 
        CAST((elapsedTime * 100.0) / (:activityDuration) AS INT) AS bucketNumber,
        AVG(hr.heartRate) AS avgHr,
        MIN(hr.elapsedTime) AS elapsedTime
    FROM HeartRateBleEntity hr
    WHERE hr.activityId = :activityId
    GROUP BY bucketNumber
    ORDER BY bucketNumber
"""
    )
    suspend fun getAggregatedHeartRateForActivity(
        activityId: String,
        activityDuration: Long
    ): List<AvgHrBucketByActivity>

    @Query("DELETE from HeartRateBleEntity where activityId in (:ids)")
    suspend fun deleteRecordsByIds(ids: Set<String>)
}
