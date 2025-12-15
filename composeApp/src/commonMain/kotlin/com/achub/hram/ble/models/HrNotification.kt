package com.achub.hram.ble.models

data class HrNotification(
    val hrBpm: Int,
    val isSensorContactSupported: Boolean,
    val isContactOn: Boolean,
)
