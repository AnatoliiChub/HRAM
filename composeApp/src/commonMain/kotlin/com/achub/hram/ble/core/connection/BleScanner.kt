package com.achub.hram.ble.core.connection

import com.juul.kable.Advertisement
import com.juul.kable.Identifier
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration
import kotlin.uuid.ExperimentalUuidApi

/**
 * Abstraction for performing Bluetooth Low Energy (BLE) scans.
 */
interface BleScanner {
    /**
     * Scans for a specific BLE device identified by [identifier] for a given [duration].
     *
     * @param identifier The unique identifier of the BLE device to scan for.
     * @param duration The duration to perform the scan.
     * @return The [Advertisement] of the found device.
     */
    @OptIn(ExperimentalUuidApi::class)
    suspend fun scan(identifier: Identifier, duration: Duration): Advertisement

    /**
     * Scans for BLE advertisements.
     *
     * @return A [Flow] emitting [Advertisement]s found during the scan.
     */
    fun scan(): Flow<Advertisement>
}
