package com.achub.hram.data

import com.achub.hram.data.db.dao.ActivityDao
import com.achub.hram.data.db.dao.HeartRateDao
import com.achub.hram.data.db.entity.ActivityEntity
import com.achub.hram.data.db.entity.ActivityGraphInfo
import com.achub.hram.data.db.entity.ActivityWithHeartRates
import com.achub.hram.data.db.entity.HeartRateEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import org.koin.core.annotation.Provided

@OptIn(ExperimentalCoroutinesApi::class)
class HramHrActivityRepo(@Provided val actDao: ActivityDao, @Provided val hrDao: HeartRateDao) : HrActivityRepo {

    override suspend fun insert(item: HeartRateEntity) = hrDao.insert(item)

    override suspend fun insert(item: ActivityEntity) = actDao.insert(item)

    override suspend fun updateByName(name: String, newName: String, duration: Long) =
        actDao.updateByName(name = name, newName = newName, duration = duration)

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
                        }
                    )
                }
            }

            combine(flows) { it.toList() }
        } else flowOf(emptyList())
    }

    override fun getActivityWithHeartRates(id: String): Flow<ActivityWithHeartRates> =
        actDao.getActivityWithHeartRates(id).filterNotNull()
}
