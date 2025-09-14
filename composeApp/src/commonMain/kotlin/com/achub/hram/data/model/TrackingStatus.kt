package com.achub.hram.data.model

data class TrackingStatus(
    val trackHR: Boolean = false,
    val trackGps: Boolean = false,
    val hrDevice: String? = null
) {
    val atLeastOneTrackingEnabled: Boolean
        get() = trackHR || trackGps
}