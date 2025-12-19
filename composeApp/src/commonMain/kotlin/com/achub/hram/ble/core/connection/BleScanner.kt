package com.achub.hram.ble.core.connection

import com.juul.kable.Advertisement
import com.juul.kable.Identifier
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration
import kotlin.uuid.ExperimentalUuidApi

interface BleScanner {
    @OptIn(ExperimentalUuidApi::class)
    suspend fun scan(identifier: Identifier, duration: Duration): Advertisement

    fun scan(): Flow<Advertisement>
}
