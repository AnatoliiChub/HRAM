package com.achub.hram.data.models

import com.achub.hram.ble.models.BleDevice

data class TrackingStatus(
    val trackHR: Boolean = false,
    val trackGps: Boolean = false,
    val hrDevice: BleDevice? = null
) {
    val atLeastOneTrackingEnabled: Boolean
        get() = trackHR || trackGps
}
