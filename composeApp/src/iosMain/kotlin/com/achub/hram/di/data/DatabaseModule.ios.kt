package com.achub.hram.di.data

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.NativeSQLiteDriver
import com.achub.hram.data.db.HramDatabase
import com.achub.hram.data.db.getRoomDatabase
import com.achub.hram.di.WorkerIOThread
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single
import org.koin.core.scope.Scope
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@Module
@Configuration
actual class DatabaseModule actual constructor() {
    @Single
    actual fun provideDatabaseBuilder(
        scope: Scope,
        @WorkerIOThread dispatcher: CoroutineDispatcher
    ): RoomDatabase.Builder<HramDatabase> = getDatabaseBuilder(dispatcher)

    @Single
    actual fun provideDatabase(
        @Provided builder: RoomDatabase.Builder<HramDatabase>,
        @WorkerIOThread dispatcher: CoroutineDispatcher
    ): HramDatabase =
        getRoomDatabase(builder, dispatcher)
}

fun getDatabaseBuilder(dispatcher: CoroutineDispatcher): RoomDatabase.Builder<HramDatabase> {
    val dbFilePath = documentDirectory() + "/hram_room.db"
    return Room.databaseBuilder<HramDatabase>(
        name = dbFilePath,
    ).setDriver(NativeSQLiteDriver()).setQueryCoroutineContext(dispatcher)
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
