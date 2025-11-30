package com.achub.hram.di.data

import androidx.room.RoomDatabase
import com.achub.hram.data.db.HramDatabase
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single
import org.koin.core.scope.Scope

@Configuration
@Module
expect class DatabaseModule() {
    @Single
    fun provideDatabaseBuilder(scope: Scope): RoomDatabase.Builder<HramDatabase>

    @Single
    fun provideDatabase(
        @Provided builder: RoomDatabase.Builder<HramDatabase>
    ): HramDatabase
}
