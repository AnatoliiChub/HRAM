package com.achub.hram.tracking

import com.achub.hram.data.models.BleState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow

interface LiveActivityManager {
    @OptIn(FlowPreview::class)
    fun startObserving(bleStateFlow: Flow<BleState>, trackingStateFlow: Flow<TrackingStateStage>)

    fun stopObserving()

    fun startActivity(bleState: BleState, trackingStateStage: TrackingStateStage)

    fun cleanup()
}
