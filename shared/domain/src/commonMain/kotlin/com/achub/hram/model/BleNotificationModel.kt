package com.achub.hram.model

import kotlinx.serialization.Serializable

@Serializable
data class BleNotificationModel(
    val hrNotification: HrNotificationModel? = null,
    val batteryLevel: Int,
    val isBleConnected: Boolean,
    val elapsedTime: Long = 0L,
) {
    companion object {
        val Empty = BleNotificationModel(null, 0, false, 0L)
    }

    fun isEmpty() = this == Empty
}

