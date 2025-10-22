package com.achub.hram.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import com.achub.hram.data.db.entity.AvgHrBucketByActivity
import com.achub.hram.data.db.entity.HeartRateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HeartRateDao {
    @Insert(onConflict = REPLACE)
    suspend fun insert(item: HeartRateEntity)

    @Query("SELECT * FROM HeartRateEntity")
    fun getAll(): Flow<List<HeartRateEntity>>

    @Query(
        """
    SELECT 
        CAST((timeStamp * 100.0) / :activityDuration AS INT) AS bucketNumber,
        AVG(hr.heartRate) AS avgHr
    FROM HeartRateEntity hr
    WHERE hr.activityId = :activityId
    GROUP BY bucketNumber
    ORDER BY bucketNumber
"""
    )
    fun getAggregatedHeartRateForActivity(
        activityId: String,
        activityDuration: Long
    ): Flow<List<AvgHrBucketByActivity>>
}
