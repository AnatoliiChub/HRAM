package com.achub.hram.model

sealed interface ConnectionResultModel {
    data class Connected(val device: DeviceModel) : ConnectionResultModel
    data class Error(val error: Throwable) : ConnectionResultModel
}

