package com.achub.hram.models

import kotlinx.serialization.Serializable

@Serializable
data class HrNotificationModel(
    val hrBpm: Int,
    val isSensorContactSupported: Boolean,
    val isContactOn: Boolean,
)
