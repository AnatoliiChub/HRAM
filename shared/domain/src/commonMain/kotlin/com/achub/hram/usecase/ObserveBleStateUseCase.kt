package com.achub.hram.usecase

import com.achub.hram.data.state.BleStateRepo
import com.achub.hram.models.BleState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart

class ObserveBleStateUseCase(private val bleStateRepo: BleStateRepo) {
    operator fun invoke(): Flow<BleState> = bleStateRepo.listen().onStart { bleStateRepo.release() }
}
