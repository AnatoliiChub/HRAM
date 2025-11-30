package com.achub.hram.ble.model

data class HrNotification(
    val hrBpm: Int,
    val isSensorContactSupported: Boolean,
    val isContactOn: Boolean,
)
