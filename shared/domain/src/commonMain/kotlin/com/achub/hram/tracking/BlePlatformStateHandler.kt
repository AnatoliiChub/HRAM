package com.achub.hram.tracking

import com.achub.hram.models.ScanError

/**
 * Abstracts platform-specific knowledge of BLE error conditions.
 * Implementations live in :presentation where platform SDKs (e.g. moko) are available.
 */
interface BlePlatformStateHandler {
    /**
     * Maps a platform-specific scan exception to a [ScanError], or returns null
     * if the exception is not platform-specific (domain handles it with a fallback).
     */
    fun mapScanError(exception: Throwable): ScanError?
}
