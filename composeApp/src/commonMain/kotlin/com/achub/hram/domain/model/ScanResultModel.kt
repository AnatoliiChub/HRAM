package com.achub.hram.domain.model

sealed interface ScanResultModel {
    data class ScanUpdate(val device: DeviceModel) : ScanResultModel
    data class Error(val error: Throwable) : ScanResultModel
    data object Complete : ScanResultModel
    data object Initiated : ScanResultModel
}
