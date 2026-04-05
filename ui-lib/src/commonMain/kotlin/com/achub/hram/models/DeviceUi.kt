package com.achub.hram.models

data class DeviceUi(
    val name: String,
    val identifier: String,
    val manufacturer: String? = null,
)
