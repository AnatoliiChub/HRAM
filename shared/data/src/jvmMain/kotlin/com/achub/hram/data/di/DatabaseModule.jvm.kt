package com.achub.hram.data.di

import androidx.room.Room
import androidx.room.RoomDatabase
import com.achub.hram.data.db.HramDatabase
import com.achub.hram.data.db.getRoomDatabase
import com.achub.hram.di.CoroutineModule
import com.achub.hram.di.WorkerIOThread
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single
import org.koin.core.scope.Scope
import java.io.File

@Module(includes = [CoroutineModule::class])
@Configuration
actual class DatabaseModule actual constructor() {
    @Single
    actual fun provideDatabaseBuilder(
        scope: Scope,
        @WorkerIOThread dispatcher: CoroutineDispatcher,
    ): RoomDatabase.Builder<HramDatabase> {
        val dbDir = File(System.getProperty("user.home"), ".hram")
        dbDir.mkdirs()
        val dbFile = File(dbDir, "hram_room.db")
        return Room.databaseBuilder<HramDatabase>(name = dbFile.absolutePath)
    }

    @Single
    actual fun provideDatabase(
        @Provided builder: RoomDatabase.Builder<HramDatabase>,
        @WorkerIOThread dispatcher: CoroutineDispatcher,
    ): HramDatabase = getRoomDatabase(builder, dispatcher)
}
