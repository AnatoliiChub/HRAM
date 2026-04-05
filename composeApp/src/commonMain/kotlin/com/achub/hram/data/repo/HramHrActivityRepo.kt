package com.achub.hram.data.repo

import com.achub.hram.data.db.dao.ActivityDao
import com.achub.hram.data.db.dao.HeartRateDao
import com.achub.hram.data.db.entity.ActivityEntity
import com.achub.hram.data.db.entity.ActivityWithHeartRates
import com.achub.hram.data.db.entity.HeartRateBleEntity
import com.achub.hram.models.GraphLimitsUi
import com.achub.hram.ext.logger
import com.achub.hram.view.cards.ActivityGraphInfo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import org.koin.core.annotation.Provided

private const val TAG = "HramHrActivityRepo"
private const val MAX_HR_FACTOR = 1.2f

@OptIn(ExperimentalCoroutinesApi::class)
class HramHrActivityRepo(
    @Provided val actDao: ActivityDao,
    @Provided val hrDao: HeartRateDao
) : HrActivityRepo {
    override suspend fun insert(item: HeartRateBleEntity) = hrDao.insert(item)

    override suspend fun insert(item: ActivityEntity) = actDao.insert(item)

    override suspend fun updateById(id: String, name: String, duration: Long) =
        actDao.updateNameById(id = id, name = name, duration = duration)

    override suspend fun updateById(id: String, name: String) {
        actDao.updateNameById(id = id, name = name)
    }

    override suspend fun getActivity(id: String) = actDao.getActivity(id)

    override fun getActivitiesGraph(): Flow<List<ActivityGraphInfo>> = actDao.getAll().flatMapLatest { activities ->
        flowOf(
            if (activities.isNotEmpty()) {
                activities.map { activity ->
                    val all = hrDao.getAllForActivity(activity.id)
                    val aggregated = hrDao.getAggregatedHeartRateForActivity(activity.id, activity.duration)
                        .map { bucket -> bucket.copy(elapsedTime = bucket.elapsedTime / 1000) }
                    ActivityGraphInfo(
                        duration =  activity.duration / 1000L,
                        id = activity.id,
                        name = activity.name,
                        startDate = activity.startDate,
                        buckets = aggregated,
                        totalRecords = all.count {
                            it.activityId == activity.id
                        },
                        limits = GraphLimitsUi(
                            0f,
                            aggregated.maxOfOrNull { it.elapsedTime }?.toFloat() ?: 1f,
                            0f,
                            (aggregated.maxOfOrNull { it.avgHr } ?: 1f) * MAX_HR_FACTOR
                        ),
                        avgHr = if (aggregated.isNotEmpty()) {
                            aggregated.map { it.avgHr }.average().toInt()
                        } else {
                            0
                        },
                        maxHr = aggregated.maxOfOrNull { it.avgHr }?.toInt() ?: 0,
                        minHr = aggregated.minOfOrNull { it.avgHr }?.toInt() ?: 0,
                    )
                }.onEach {
                    logger(
                        TAG
                    ) { "ActivityGraphInfo for ${it.name}: ${it.limits}, size: ${it.buckets.size}" }
                }
            } else {
                emptyList()
            }
        )
    }

    override fun getActivityWithHeartRates(id: String): Flow<ActivityWithHeartRates> =
        actDao.getActivityWithHeartRates(id).filterNotNull()

    override suspend fun getHeartRatesForActivity(activityId: String): List<HeartRateBleEntity> =
        hrDao.getAllForActivity(activityId)

    override suspend fun deleteActivitiesById(ids: Set<String>) {
        actDao.deleteByIds(ids)
        hrDao.deleteRecordsByIds(ids)
    }
}
