package com.achub.hram.ble

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class BluetoothObserverNoOp : BluetoothObserver {
    override fun observeBleState(): Flow<Boolean> = flow {
        emit(true)
        awaitCancellation()
    }
}
