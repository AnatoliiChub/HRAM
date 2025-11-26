package com.achub.hram.data.models

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

data class HrIndication(
    val hrBpm: Int,
    val isSensorContactSupported: Boolean,
    val isContactOn: Boolean,
)
