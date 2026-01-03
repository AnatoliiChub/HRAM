package com.achub.hram.ble.models

import kotlinx.serialization.Serializable

@Serializable
data class BleNotification(
    val hrNotification: HrNotification? = null,
    val batteryLevel: Int,
    val isBleConnected: Boolean,
    val elapsedTime: Long = 0L,
) {
    companion object Companion {
        val Empty = BleNotification(null, 0, false, 0L)
    }

    fun isEmpty() = this == Empty
}
