package com.achub.hram.data.store

import androidx.datastore.core.okio.OkioSerializer
import com.achub.hram.models.UserSettings
import kotlinx.serialization.json.Json
import okio.BufferedSink
import okio.BufferedSource
import okio.use

class UserSettingsSerializer(val json: Json) : OkioSerializer<UserSettings> {
    override val defaultValue: UserSettings = UserSettings.Default

    override suspend fun readFrom(source: BufferedSource): UserSettings =
        try {
            json.decodeFromString<UserSettings>(source.readUtf8())
        } catch (e: Exception) {
            defaultValue
        }

    override suspend fun writeTo(t: UserSettings, sink: BufferedSink) {
        sink.use { it.writeUtf8(json.encodeToString(UserSettings.serializer(), t)) }
    }
}
