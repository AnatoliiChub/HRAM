package com.achub.hram.ble.models

import kotlinx.serialization.Serializable

@Serializable
data class HrNotification(
    val hrBpm: Int,
    val isSensorContactSupported: Boolean,
    val isContactOn: Boolean,
)
