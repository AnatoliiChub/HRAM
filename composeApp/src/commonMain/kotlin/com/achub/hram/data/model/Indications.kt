package com.achub.hram.data.model

import kotlin.time.ExperimentalTime

data class Indications(
    val hrIndication: HrIndication = HrIndication.Empty,
    val distance: Float = 0f,
    val duration: Long = 0L,
)

data class HrIndication(val hrBpm: Int, val batteryLevel: Int) {

    companion object Companion {
        const val NO_HR_RATE = -1
        const val NO_BATTERY_LEVEL = -1

        @OptIn(ExperimentalTime::class)
        val Empty = HrIndication(NO_HR_RATE, NO_BATTERY_LEVEL)
    }
    fun isEmpty() = this == Empty
}
