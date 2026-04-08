package com.achub.hram.tracking.notification

import com.achub.hram.models.BleState
import com.achub.hram.tracking.TrackingStateStage
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow

interface LiveActivityManager {
    @OptIn(FlowPreview::class)
    fun startObserving(bleStateFlow: Flow<BleState>, trackingStateFlow: Flow<TrackingStateStage>)

    fun stopObserving()

    fun startActivity(bleState: BleState, trackingStateStage: TrackingStateStage)

    fun cleanup()
}
