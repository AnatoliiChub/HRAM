package com.achub.hram.data

import com.achub.hram.data.db.dao.ActivityDao
import com.achub.hram.data.db.dao.HeartRateDao
import com.achub.hram.data.db.entity.ActivityEntity
import com.achub.hram.data.db.entity.ActivityGraphInfo
import com.achub.hram.data.db.entity.ActivityWithHeartRates
import com.achub.hram.data.db.entity.HeartRateEntity
import com.achub.hram.data.models.GraphLimits
import com.achub.hram.logger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import org.koin.core.annotation.Provided

private const val TAG = "HramHrActivityRepo"

@OptIn(ExperimentalCoroutinesApi::class)
class HramHrActivityRepo(@Provided val actDao: ActivityDao, @Provided val hrDao: HeartRateDao) : HrActivityRepo {

    override suspend fun insert(item: HeartRateEntity) = hrDao.insert(item)

    override suspend fun insert(item: ActivityEntity) = actDao.insert(item)

    override suspend fun updateNameById(id: String, name: String, duration: Long) =
        actDao.updateNameById(id = id, name = name, duration = duration)

    override suspend fun updateNameById(id: String, name: String) {
        actDao.updateNameById(id = id, name = name)
    }

    override fun getActivityByName(name: String): ActivityEntity? = getActivityByName(name)

    override fun getActivitiesGraph(): Flow<List<ActivityGraphInfo>> = actDao.getAll().flatMapLatest { activities ->
        if (activities.isNotEmpty()) {
            val flows = activities.map { activity ->
                combine(
                    hrDao.getAggregatedHeartRateForActivity(activity.id, activity.duration),
                    hrDao.getAll()
                ) { aggregated, all ->
                    ActivityGraphInfo(
                        activity = activity,
                        buckets = aggregated,
                        totalRecords = all.count {
                            it.activityId == activity.id
                        },
                        limits = GraphLimits(
                            0f,
                            aggregated.maxOfOrNull { it.timestamp }?.toFloat() ?: 1f,
                            0f,
                            (aggregated.maxOfOrNull { it.avgHr } ?: 1f) * 1.2f
                        ),
                        avgHr = if (aggregated.isNotEmpty()) {
                            aggregated.map { it.avgHr }.average().toInt()
                        } else 0,
                        maxHr = aggregated.maxOfOrNull { it.avgHr }?.toInt() ?: 0,
                        minHr = aggregated.minOfOrNull { it.avgHr }?.toInt() ?: 0,
                    )
                }.onEach { logger(TAG) { "ActivityGraphInfo for ${activity.name}: ${it.limits}" } }
            }

            combine(flows) { it.toList() }
        } else flowOf(emptyList())
    }

    override fun getActivityWithHeartRates(id: String): Flow<ActivityWithHeartRates> =
        actDao.getActivityWithHeartRates(id).filterNotNull()

    override suspend fun deleteActivitiesById(ids: Set<String>) {
        actDao.deleteByIds(ids)
        hrDao.deleteRecordsByIds(ids)
    }
}
