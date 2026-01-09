package com.achub.hram.data.store

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioSerializer
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.core.okio.createSingleProcessCoordinator
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM

fun <T> createOkioDataStore(
    produceFilePath: () -> String,
    serializer: OkioSerializer<T>
): DataStore<T> = DataStoreFactory.create(
    storage = OkioStorage(
        fileSystem = FileSystem.SYSTEM,
        serializer = serializer,
        producePath = { produceFilePath().toPath() },
        coordinatorProducer = { path, _ -> createSingleProcessCoordinator(path) }
    ),
)

internal const val BLE_STATE_FILE_NAME = "hram.ble_state.json"
internal const val TRACKING_STATE_STAGE_FILE_NAME = "hram.tracking_state_stage.json"
