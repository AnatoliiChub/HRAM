package com.achub.hram.models

data class BleNotificationUi(
    val hrNotification: HrNotificationUi? = null,
    val batteryLevel: Int,
    val isBleConnected: Boolean,
    val elapsedTime: Long = 0L,
) {
    companion object {
        val Empty = BleNotificationUi(null, 0, false, 0L)
    }

    fun isEmpty() = this == Empty
}

