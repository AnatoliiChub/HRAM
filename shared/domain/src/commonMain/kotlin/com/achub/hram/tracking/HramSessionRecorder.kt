package com.achub.hram.tracking

import com.achub.hram.data.HrActivityRepo
import com.achub.hram.data.state.TrackingStateRepo
import com.achub.hram.di.WorkerThread
import com.achub.hram.ext.cancelAndClear
import com.achub.hram.ext.createActivity
import com.achub.hram.models.ACTIVE_ACTIVITY
import com.achub.hram.models.BleNotificationModel
import com.achub.hram.models.HeartRateRecord
import com.achub.hram.tracking.stopwatch.StopWatch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Clock.System.now
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalAtomicApi::class, ExperimentalUuidApi::class, ExperimentalTime::class)
class HramSessionRecorder(
    @param:WorkerThread
    private val dispatcher: CoroutineDispatcher,
    private val stopWatch: StopWatch,
    private val hrActivityRepo: HrActivityRepo,
    private val trackingStateRepo: TrackingStateRepo,
) : SessionRecorder {
    private val scope = CoroutineScope(dispatcher + SupervisorJob())
    private val jobs = mutableListOf<Job>()
    private var currentActId: String? = null

    override fun startTracking() {
        scope.launch(dispatcher) {
            if (currentActId == null) {
                val currentTime = now().epochSeconds
                val activity = createActivity(ACTIVE_ACTIVITY, currentTime)
                currentActId = activity.id
                hrActivityRepo.insert(activity)
            }
            trackingStateRepo.update(TrackingStateStage.ACTIVE_TRACKING_STATE)
            stopWatch.start()
        }.let { jobs.add(it) }
    }

    override fun pauseTracking() {
        scope.launch { trackingStateRepo.update(TrackingStateStage.PAUSED_TRACKING_STATE) }
        stopWatch.pause()
    }

    override fun finishTracking(name: String?) {
        scope.launch(dispatcher) {
            trackingStateRepo.update(TrackingStateStage.TRACKING_INIT_STATE)
            val duration = stopWatch.elapsedTime()
            stopWatch.reset()
            val newName = name ?: "${now().epochSeconds}__$duration"
            currentActId?.let { hrActivityRepo.updateById(id = it, name = newName, duration = duration) }
            currentActId = null
        }.let { jobs.add(it) }
    }

    override suspend fun record(notification: BleNotificationModel) {
        notification.hrNotification?.let { hrNotification ->
            val isContactOn = if (hrNotification.isSensorContactSupported) hrNotification.isContactOn else true
            currentActId?.let {
                hrActivityRepo.insert(
                    HeartRateRecord(
                        activityId = it,
                        heartRate = hrNotification.hrBpm,
                        elapsedTime = notification.elapsedTime,
                        isContactOn = isContactOn,
                        batteryLevel = notification.batteryLevel,
                        timestamp = now().toEpochMilliseconds(),
                    )
                )
            }
        }
    }

    override fun releaseState() {
        scope.launch { trackingStateRepo.release() }
    }

    override suspend fun trackingState() = trackingStateRepo.get()

    override suspend fun isTracking() = trackingStateRepo.get() == TrackingStateStage.ACTIVE_TRACKING_STATE

    override fun elapsedTime() = stopWatch.elapsedTime()

    override fun cancelJobs() = jobs.cancelAndClear()
}
