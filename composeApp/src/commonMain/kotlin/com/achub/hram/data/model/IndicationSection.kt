package com.achub.hram.data.model

import kotlin.time.ExperimentalTime

data class IndicationSection(
    val hrNotifications: HrNotifications = HrNotifications.Empty,
    val distance: Float = 0f,
    val duration: String = "00:00:00",
)

data class HrNotifications(val hrBpm: Int, val batteryLevel: Int, val timestamp: Long = 0) {

    companion object Companion {
        const val NO_HR_RATE = -1
        const val NO_BATTERY_LEVEL = -1

        @OptIn(ExperimentalTime::class)
        val Empty = HrNotifications(NO_HR_RATE, NO_BATTERY_LEVEL)
    }
    fun isEmpty() = this == Empty
}
