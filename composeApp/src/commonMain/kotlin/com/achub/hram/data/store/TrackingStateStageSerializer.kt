package com.achub.hram.data.store

import androidx.datastore.core.okio.OkioSerializer
import com.achub.hram.tracking.TrackingStateStage
import kotlinx.serialization.json.Json
import okio.BufferedSink
import okio.BufferedSource
import okio.use

class TrackingStateStageSerializer(val json: Json) : OkioSerializer<TrackingStateStage> {
    override val defaultValue: TrackingStateStage = TrackingStateStage.TRACKING_INIT_STATE

    override suspend fun readFrom(source: BufferedSource): TrackingStateStage =
        json.decodeFromString<TrackingStateStage>(source.readUtf8())

    override suspend fun writeTo(t: TrackingStateStage, sink: BufferedSink) {
        sink.use { it.writeUtf8(json.encodeToString(TrackingStateStage.serializer(), t)) }
    }
}
