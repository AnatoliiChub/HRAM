package com.achub.hram.tracking

import com.achub.hram.models.ScanError

/** Used on platforms where moko-permissions is not available (e.g. JVM desktop). */
class NoOpBlePlatformStateHandler : BlePlatformStateHandler {
    override fun mapScanError(exception: Throwable): ScanError? = null
}

