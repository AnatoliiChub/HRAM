package com.achub.hram.data

import androidx.datastore.core.okio.OkioSerializer
import com.achub.hram.data.models.BleState
import kotlinx.serialization.json.Json
import okio.BufferedSink
import okio.BufferedSource
import okio.use

class TrackingStateSerializer(val json: Json) : OkioSerializer<BleState> {
    override val defaultValue: BleState = BleState.Disconnected

    override suspend fun readFrom(source: BufferedSource): BleState = json.decodeFromString<BleState>(
        source.readUtf8()
    )

    override suspend fun writeTo(t: BleState, sink: BufferedSink) {
        sink.use { it.writeUtf8(json.encodeToString(BleState.serializer(), t)) }
    }
}
