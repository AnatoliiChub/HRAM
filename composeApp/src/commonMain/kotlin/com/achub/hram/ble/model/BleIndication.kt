package com.achub.hram.ble.model

data class BleIndication(
    val hrIndication: HrIndication? = null,
    val batteryLevel: Int,
    val isBleConnected: Boolean,
    val elapsedTime: Long = 0L,
) {
    companion object Companion {
        val Empty = BleIndication(null, 0, false, 0L)
    }

    fun isEmpty() = this == Empty
}
