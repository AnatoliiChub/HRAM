package com.achub.hram.ble.model

data class HrIndication(
    val hrBpm: Int,
    val isSensorContactSupported: Boolean,
    val isContactOn: Boolean,
)