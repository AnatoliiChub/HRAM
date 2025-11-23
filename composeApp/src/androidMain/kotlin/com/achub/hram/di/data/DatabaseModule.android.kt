package com.achub.hram.di.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.achub.hram.data.db.HramDatabase
import com.achub.hram.data.db.getRoomDatabase
import org.koin.core.annotation.Module
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single
import org.koin.core.scope.Scope

@Module
actual class DatabaseModule actual constructor() {

    @Single
    actual fun provideDatabaseBuilder(scope: Scope): RoomDatabase.Builder<HramDatabase> =
        getDatabaseBuilder(scope.get())

    @Single
    actual fun provideDatabase(@Provided builder: RoomDatabase.Builder<HramDatabase>): HramDatabase =
        getRoomDatabase(builder)
}

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<HramDatabase> {
    val dbFile = context.getDatabasePath("hram_room.db")
    return Room.databaseBuilder<HramDatabase>(
        context = context,
        name = dbFile.absolutePath
    )
}
