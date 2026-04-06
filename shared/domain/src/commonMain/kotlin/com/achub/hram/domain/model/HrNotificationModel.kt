package com.achub.hram.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class HrNotificationModel(
    val hrBpm: Int,
    val isSensorContactSupported: Boolean,
    val isContactOn: Boolean,
)

