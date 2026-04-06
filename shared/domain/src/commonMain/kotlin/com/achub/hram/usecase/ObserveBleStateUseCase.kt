package com.achub.hram.usecase

import com.achub.hram.data.models.BleState
import com.achub.hram.data.repo.state.BleStateRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart

class ObserveBleStateUseCase(private val bleStateRepo: BleStateRepo) {
    operator fun invoke(): Flow<BleState> = bleStateRepo.listen().onStart { bleStateRepo.release() }
}
