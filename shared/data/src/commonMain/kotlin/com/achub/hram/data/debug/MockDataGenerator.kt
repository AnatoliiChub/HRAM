package com.achub.hram.data.debug

import com.achub.hram.data.db.dao.ActivityDao
import com.achub.hram.data.db.dao.HeartRateDao
import com.achub.hram.data.db.entity.ActivityEntity
import com.achub.hram.data.db.entity.HeartRateBleEntity
import kotlinx.coroutines.flow.first
import kotlin.random.Random
import kotlin.time.Clock.System.now
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val TARGET_ACTIVITY_COUNT = 100
private const val MIN_DURATION_MINUTES = 15
private const val MAX_DURATION_MINUTES = 31
private const val SECONDS_IN_MINUTE = 60
private const val SECONDS_IN_HOUR = 3600
private const val MILLIS_IN_SECOND = 1000
private const val MIN_BASE_HR = 70
private const val MAX_BASE_HR = 140
private const val HR_SAMPLING_RATE_SECONDS = 2L
private const val MIN_HR_VARIATION = -3
private const val MAX_HR_VARIATION = 4
private const val MIN_HR_LIMIT = 50
private const val MAX_HR_LIMIT = 200
private const val MOCK_BATTERY_LEVEL = 95

@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
class MockDataGenerator(
    private val actDao: ActivityDao,
    private val hrDao: HeartRateDao
) {
    suspend fun generateIfNeeded() {
        val existingCount = actDao.getAll().first().size
        if (existingCount < TARGET_ACTIVITY_COUNT) {
            val toGenerate = TARGET_ACTIVITY_COUNT - existingCount
            val baseTime = now().epochSeconds

            repeat(toGenerate) { i ->
                val activityId = Uuid.random().toString()
                val durationMinutes = Random.nextInt(MIN_DURATION_MINUTES, MAX_DURATION_MINUTES)
                val durationSeconds = (durationMinutes * SECONDS_IN_MINUTE).toLong()
                val startDate = baseTime - (i * SECONDS_IN_HOUR) // Spread activities hourly in the past

                val activity = ActivityEntity(
                    id = activityId,
                    name = "Mock Run #${i + 1}",
                    duration = durationSeconds * MILLIS_IN_SECOND,
                    startDate = startDate
                )
                actDao.insert(activity)

                val hrRecords = mutableListOf<HeartRateBleEntity>()
                val baseHr = Random.nextInt(MIN_BASE_HR, MAX_BASE_HR)

                // Generate one heart rate record every 2 seconds
                for (elapsed in 0 until durationSeconds step HR_SAMPLING_RATE_SECONDS) {
                    val variation = Random.nextInt(MIN_HR_VARIATION, MAX_HR_VARIATION)
                    val hr = (baseHr + variation + (elapsed / SECONDS_IN_MINUTE)).toInt() // slight drift upwards
                    hrRecords.add(
                        HeartRateBleEntity(
                            activityId = activityId,
                            heartRate = hr.coerceIn(MIN_HR_LIMIT, MAX_HR_LIMIT),
                            timestamp = (startDate + elapsed) * MILLIS_IN_SECOND,
                            elapsedTime = elapsed * MILLIS_IN_SECOND,
                            isContactOn = true,
                            batteryLevel = MOCK_BATTERY_LEVEL
                        )
                    )
                }
                hrDao.insertAll(hrRecords)
            }
        }
    }
}
