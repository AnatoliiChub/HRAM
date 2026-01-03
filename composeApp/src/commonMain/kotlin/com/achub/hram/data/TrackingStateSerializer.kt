package com.achub.hram.data

import androidx.datastore.core.okio.OkioSerializer
import com.achub.hram.data.models.TrackingState
import kotlinx.serialization.json.Json
import okio.BufferedSink
import okio.BufferedSource
import okio.use

class TrackingStateSerializer(val json: Json) : OkioSerializer<TrackingState> {
    override val defaultValue: TrackingState = TrackingState.Disconnected

    override suspend fun readFrom(source: BufferedSource): TrackingState = json.decodeFromString<TrackingState>(
        source.readUtf8()
    )

    override suspend fun writeTo(t: TrackingState, sink: BufferedSink) {
        sink.use { it.writeUtf8(json.encodeToString(TrackingState.serializer(), t)) }
    }
}
