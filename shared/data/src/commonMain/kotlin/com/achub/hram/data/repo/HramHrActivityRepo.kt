package com.achub.hram.data.repo

import com.achub.hram.data.db.dao.ActivityDao
import com.achub.hram.data.db.dao.HeartRateDao
import com.achub.hram.data.mapper.toDomain
import com.achub.hram.data.mapper.toEntity
import com.achub.hram.domain.model.ActivityInfo
import com.achub.hram.domain.model.ActivityRecord
import com.achub.hram.domain.model.HeartRateRecord
import com.achub.hram.domain.model.HrBucket
import com.achub.hram.Logger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import org.koin.core.annotation.Provided

private const val TAG = "HramHrActivityRepo"

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

    override fun getActivities(): Flow<List<ActivityInfo>> = actDao.getAll().flatMapLatest { activities ->
        flowOf(
            if (activities.isNotEmpty()) {
                activities.map { activity ->
                    val all = hrDao.getAllForActivity(activity.id)
                    val aggregated = hrDao.getAggregatedHeartRateForActivity(activity.id, activity.duration)
                        .map { bucket -> HrBucket(bucket.bucketNumber, bucket.avgHr, bucket.elapsedTime / 1000) }
                    ActivityInfo(
                        id = activity.id,
                        name = activity.name,
                        startDate = activity.startDate,
                        duration = activity.duration / 1000L,
                        buckets = aggregated,
                        totalRecords = all.count { it.activityId == activity.id },
                        avgHr = if (aggregated.isNotEmpty()) aggregated.map { it.avgHr }.average().toInt() else 0,
                        maxHr = aggregated.maxOfOrNull { it.avgHr }?.toInt() ?: 0,
                        minHr = aggregated.minOfOrNull { it.avgHr }?.toInt() ?: 0,
                    )
                }.onEach {
                    Logger.D(TAG) { "ActivityInfo for ${it.name}: size=${it.buckets.size}" }
                }
            } else {
                emptyList()
            }
        )
    }

    override suspend fun getHeartRatesForActivity(activityId: String): List<HeartRateRecord> =
        hrDao.getAllForActivity(activityId).map { it.toDomain() }

    override suspend fun deleteActivitiesById(ids: Set<String>) {
        actDao.deleteByIds(ids)
        hrDao.deleteRecordsByIds(ids)
    }
}
