package com.achub.hram.tracking

import kotlinx.serialization.Serializable

@Serializable
enum class TrackingStateStage {
    TRACKING_INIT_STATE,
    ACTIVE_TRACKING_STATE,
    PAUSED_TRACKING_STATE;

    fun isActive() = this == ACTIVE_TRACKING_STATE
}
