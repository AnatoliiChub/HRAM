package com.achub.hram.data.repo

import com.achub.hram.ble.HrDeviceRepo
import com.achub.hram.ble.models.BleConnectionsException.BleUnavailableException
import com.achub.hram.data.BleDeviceRepository
import com.achub.hram.data.mapper.toBle
import com.achub.hram.data.mapper.toDomain
import com.achub.hram.models.BleNotificationModel
import com.achub.hram.models.ConnectionResultModel
import com.achub.hram.models.DeviceModel
import com.achub.hram.models.DeviceUnavailableException
import com.achub.hram.models.ScanResultModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlin.time.Duration

/**
 * Data-layer implementation of [com.achub.hram.data.BleDeviceRepository].
 * Wraps [HrDeviceRepo] (BLE) and translates between BLE types and domain types.
 * Only this class (and [BleDeviceMapper]) may import from `:ble`.
 */
class HramBleDeviceRepository(
    private val hrDeviceRepo: HrDeviceRepo,
) : BleDeviceRepository {
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
