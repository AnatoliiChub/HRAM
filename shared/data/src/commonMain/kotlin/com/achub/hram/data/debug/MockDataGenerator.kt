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

@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
class MockDataGenerator(
    private val actDao: ActivityDao,
    private val hrDao: HeartRateDao
) {
    suspend fun generateIfNeeded() {
        val existingCount = actDao.getAll().first().size
        val targetCount = 100
        if (existingCount < targetCount) {
            val toGenerate = targetCount - existingCount
            val baseTime = now().epochSeconds

            repeat(toGenerate) { i ->
                val activityId = Uuid.random().toString()
                val durationMinutes = Random.nextInt(15, 31)
                val durationSeconds = (durationMinutes * 60).toLong()
                val startDate = baseTime - (i * 3600) // Spread activities hourly in the past

                val activity = ActivityEntity(
                    id = activityId,
                    name = "Mock Run #${i + 1}",
                    duration = durationSeconds * 1000,
                    startDate = startDate
                )
                actDao.insert(activity)

                val hrRecords = mutableListOf<HeartRateBleEntity>()
                val baseHr = Random.nextInt(70, 140)

                // Generate one heart rate record every 2 seconds
                for (elapsed in 0 until durationSeconds step 2) {
                    val variation = Random.nextInt(-3, 4)
                    val hr = (baseHr + variation + (elapsed / 60)).toInt() // slight drift upwards
                    hrRecords.add(
                        HeartRateBleEntity(
                            activityId = activityId,
                            heartRate = hr.coerceIn(50, 200),
                            timestamp = (startDate + elapsed) * 1000,
                            elapsedTime = elapsed * 1000,
                            isContactOn = true,
                            batteryLevel = 95
                        )
                    )
                }
                hrDao.insertAll(hrRecords)
            }
        }
    }
}
