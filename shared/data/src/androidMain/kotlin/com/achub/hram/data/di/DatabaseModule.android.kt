package com.achub.hram.data.di

import android.content.Context
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

@Module(includes = [CoroutineModule::class])
@Configuration
actual class DatabaseModule actual constructor() {
    @Single
    actual fun provideDatabaseBuilder(
        scope: Scope,
        @WorkerIOThread dispatcher: CoroutineDispatcher
    ): RoomDatabase.Builder<HramDatabase> {
        val context: Context = scope.get()
        val dbFile = context.getDatabasePath("hram_room.db")
        return Room.databaseBuilder<HramDatabase>(context = context, name = dbFile.absolutePath)
            .setQueryCoroutineContext(dispatcher)
    }

    @Single
    actual fun provideDatabase(
        @Provided builder: RoomDatabase.Builder<HramDatabase>,
        @WorkerIOThread dispatcher: CoroutineDispatcher
    ): HramDatabase = getRoomDatabase(builder, dispatcher)
}

