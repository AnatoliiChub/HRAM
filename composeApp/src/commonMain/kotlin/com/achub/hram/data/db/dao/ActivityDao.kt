package com.achub.hram.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import com.achub.hram.data.db.entity.ActivityEntity
import com.achub.hram.data.db.entity.ActivityWithHeartRates
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {

    @Insert(onConflict = REPLACE)
    suspend fun insert(item: ActivityEntity)

    @Insert
    suspend fun insertAll(items: List<ActivityEntity>)

    @Query("UPDATE ActivityEntity SET duration = :duration, name = :newName WHERE name = :name")
    suspend fun updateByName(name: String, newName: String, duration: Long)

    @Query("SELECT * FROM ActivityEntity ORDER BY startDate DESC")
    fun getAll(): Flow<List<ActivityEntity>>

    @Transaction
    @Query("SELECT * FROM ActivityEntity WHERE id = :id LIMIT 1")
    fun getActivityWithHeartRates(id: String): Flow<ActivityWithHeartRates?>
}
