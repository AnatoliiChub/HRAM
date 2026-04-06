package com.achub.hram.data.repo

import com.achub.hram.ble.HrDeviceRepo
import com.achub.hram.ble.models.BleConnectionsException.BleUnavailableException
import com.achub.hram.data.mapper.toDomain
import com.achub.hram.data.mapper.toBle
import com.achub.hram.model.BleNotificationModel
import com.achub.hram.model.ConnectionResultModel
import com.achub.hram.model.DeviceModel
import com.achub.hram.model.DeviceUnavailableException
import com.achub.hram.model.ScanResultModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlin.time.Duration

/**
 * Data-layer implementation of [DeviceDataSource].
 * Wraps [HrDeviceRepo] (BLE) and translates between BLE types and domain types.
 * Only this class (and [BleDeviceMapper]) may import from `:ble`.
 */
class HramDeviceDataSource(
    private val hrDeviceRepo: HrDeviceRepo,
) : DeviceDataSource {

    override fun scan(duration: Duration): Flow<ScanResultModel> =
        hrDeviceRepo.scan(duration)
            .map { it.toDomain() }
            .catch { e ->
                val mapped = if (e is BleUnavailableException) DeviceUnavailableException(e.message) else e
                emit(ScanResultModel.Error(mapped))
            }

    override fun connect(device: DeviceModel): Flow<ConnectionResultModel> =
        hrDeviceRepo.connect(device.toBle())
            .map { it.toDomain() }

    override fun listen(): Flow<BleNotificationModel> =
        hrDeviceRepo.listen().map { it.toDomain() }

    override suspend fun disconnect() = hrDeviceRepo.disconnect()
}

