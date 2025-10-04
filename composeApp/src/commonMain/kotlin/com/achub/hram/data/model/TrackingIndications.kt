package com.achub.hram.data.model

data class TrackingIndications(
    val heartRate: Int = 0,
    val distance: Float = 0f,
    val duration: String = "00:00:00",
)