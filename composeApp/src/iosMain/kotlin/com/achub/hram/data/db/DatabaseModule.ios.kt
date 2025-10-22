package com.achub.hram.data.db

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.NativeSQLiteDriver
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@Module
actual class DatabaseModule actual constructor() {
    @Single
    fun provideDatabaseBuilder(): RoomDatabase.Builder<HramDatabase> = getDatabaseBuilder()

    @Single()
    fun provideDatabase(builder: RoomDatabase.Builder<HramDatabase>): HramDatabase = getRoomDatabase(builder)
}

fun getDatabaseBuilder(): RoomDatabase.Builder<HramDatabase> {
    val dbFilePath = documentDirectory() + "/hram_room.db"
    return Room.databaseBuilder<HramDatabase>(
        name = dbFilePath,
    ).setDriver(NativeSQLiteDriver()).setQueryCoroutineContext(Dispatchers.IO)
}

@OptIn(ExperimentalForeignApi::class)
private fun documentDirectory(): String {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    return requireNotNull(documentDirectory?.path)
}
