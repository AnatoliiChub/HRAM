package com.achub.hram.data.models

data class TrackingStatus(
    val trackHR: Boolean = false,
    val trackGps: Boolean = false,
    val hrDevice: BleDevice? = null
) {
    val atLeastOneTrackingEnabled: Boolean
        get() = trackHR || trackGps
}
