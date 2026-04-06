package com.achub.hram.usecase

import com.achub.hram.data.repo.state.TrackingStateRepo
import com.achub.hram.tracking.TrackingStateStage
import kotlinx.coroutines.flow.Flow

class ObserveTrackingStateUseCase(private val trackingStateRepo: TrackingStateRepo) {
    operator fun invoke(): Flow<TrackingStateStage> = trackingStateRepo.listen()
}
