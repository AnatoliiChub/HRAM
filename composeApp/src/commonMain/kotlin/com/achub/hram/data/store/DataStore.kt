package com.achub.hram.data.store

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioStorage
import com.achub.hram.data.TrackingStateSerializer
import com.achub.hram.data.models.TrackingState
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM

fun createTrackingStateDataStore(
    produceFilePath: () -> String,
    serializer: TrackingStateSerializer
): DataStore<TrackingState> = DataStoreFactory.create(
    storage = OkioStorage(
        fileSystem = FileSystem.SYSTEM,
        serializer = serializer,
        producePath = { produceFilePath().toPath() },
    ),
)

internal const val DATA_STORE_FILE_NAME = "hram.preferences_pb"
