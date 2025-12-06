package com.achub.hram.data.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.achub.hram.data.db.dao.ActivityDao
import com.achub.hram.data.db.dao.HeartRateDao
import com.achub.hram.data.db.entity.ActivityEntity
import com.achub.hram.data.db.entity.HeartRateEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(entities = [ActivityEntity::class, HeartRateEntity::class], version = 1)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class HramDatabase : RoomDatabase() {
    abstract fun getActivityDao(): ActivityDao

    abstract fun getHeartRateDao(): HeartRateDao
}

// The Room compiler generates the `actual` implementations.
@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<HramDatabase> {
    override fun initialize(): HramDatabase
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<HramDatabase>
): HramDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
