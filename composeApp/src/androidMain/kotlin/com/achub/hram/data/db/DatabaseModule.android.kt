package com.achub.hram.data.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
actual class DatabaseModule actual constructor() {

    @Single
    fun provideDatabaseBuilder(context: Context): RoomDatabase.Builder<HramDatabase> = getDatabaseBuilder(context)

    @Single()
    fun provideDatabase(builder: RoomDatabase.Builder<HramDatabase>): HramDatabase = getRoomDatabase(builder)
}

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<HramDatabase> {
    val dbFile = context.getDatabasePath("hram_room.db")
    return Room.databaseBuilder<HramDatabase>(
        context = context,
        name = dbFile.absolutePath
    )
}
