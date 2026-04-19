package com.achub.hram.data.repo

import com.achub.hram.data.HrActivityRepo
import com.achub.hram.data.db.dao.ActivityDao
import com.achub.hram.data.db.dao.HeartRateDao
import com.achub.hram.data.mapper.toDomain
import com.achub.hram.data.mapper.toEntity
import com.achub.hram.models.ActivityInfo
import com.achub.hram.models.ActivityRecord
import com.achub.hram.models.HeartRateRecord
import com.achub.hram.models.HrBucket
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Provided

private const val MILLIS_IN_SECOND = 1000

@OptIn(ExperimentalCoroutinesApi::class)
class HramHrActivityRepo(
    @Provided val actDao: ActivityDao,
    @Provided val hrDao: HeartRateDao
) : HrActivityRepo {
    override suspend fun insert(item: HeartRateRecord) = hrDao.insert(item.toEntity())

    override suspend fun insert(item: ActivityRecord) = actDao.insert(item.toEntity())

    override suspend fun updateById(id: String, name: String, duration: Long) =
        actDao.updateNameById(id = id, name = name, duration = duration)

    override suspend fun updateById(id: String, name: String) =
        actDao.updateNameById(id = id, name = name)

    override suspend fun getActivity(id: String): ActivityRecord? =
        actDao.getActivity(id)?.toDomain()

    override fun getActivities(): Flow<List<ActivityInfo>> = actDao.getAll().map { activities ->
        if (activities.isEmpty()) return@map emptyList()

        val counts = hrDao.getActivityCounts()
        val buckets = hrDao.getAllAggregatedHeartRates()

        val countsMap = counts.associate { it.activityId to it.totalRecords }
        val bucketsMap = buckets.groupBy { it.activityId }

        activities.map { activity ->
            val aggregated = (bucketsMap[activity.id] ?: emptyList()).map { bucket ->
                HrBucket(bucket.bucketNumber, bucket.avgHr, bucket.elapsedTime / MILLIS_IN_SECOND)
            }
            ActivityInfo(
                id = activity.id,
                name = activity.name,
                startDate = activity.startDate,
                duration = activity.duration / 1000L,
                buckets = aggregated,
                totalRecords = countsMap[activity.id] ?: 0,
                avgHr = if (aggregated.isNotEmpty()) aggregated.map { it.avgHr }.average().toInt() else 0,
                maxHr = aggregated.maxOfOrNull { it.avgHr }?.toInt() ?: 0,
                minHr = aggregated.minOfOrNull { it.avgHr }?.toInt() ?: 0,
            )
        }
    }

    override suspend fun getHeartRatesForActivity(activityId: String): List<HeartRateRecord> =
        hrDao.getAllForActivity(activityId).map { it.toDomain() }

    override suspend fun deleteActivitiesById(ids: Set<String>) {
        actDao.deleteByIds(ids)
        hrDao.deleteRecordsByIds(ids)
    }
}
