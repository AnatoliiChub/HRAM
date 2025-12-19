package com.achub.hram.ble.core.connection

import com.achub.hram.ext.HR_SERVICE_UUID
import com.juul.kable.Advertisement
import com.juul.kable.Identifier
import com.juul.kable.Scanner
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.timeout
import kotlin.time.Duration
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class HramBleScanner(
    val scanner: Scanner<Advertisement> = Scanner { filters { match { services = listOf(HR_SERVICE_UUID) } } }
) : BleScanner {
    @OptIn(ExperimentalUuidApi::class, FlowPreview::class)
    override suspend fun scan(identifier: Identifier, duration: Duration) =
        scan().filter { it.identifier == identifier }.timeout(duration).first()

    @OptIn(ExperimentalUuidApi::class)
    override fun scan(): Flow<Advertisement> {
        return scanner.advertisements
    }
}
