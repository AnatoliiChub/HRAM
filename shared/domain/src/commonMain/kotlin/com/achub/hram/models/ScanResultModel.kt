package com.achub.hram.models

sealed interface ScanResultModel {
    data class ScanUpdate(val device: DeviceModel) : ScanResultModel

    data class Error(val error: Throwable) : ScanResultModel

    data object Complete : ScanResultModel

    data object Initiated : ScanResultModel
}
