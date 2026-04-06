package com.achub.hram.model

import kotlinx.serialization.Serializable

@Serializable
data class DeviceModel(
    val name: String,
    val identifier: String,
    val manufacturer: String? = null,
)

