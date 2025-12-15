package com.achub.hram.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
class CoroutineModule {
    @WorkerThread
    @Single
    fun provideCoroutineDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @WorkerIOThread
    @Single
    fun provideCoroutineIODispatcher(): CoroutineDispatcher = Dispatchers.IO
}
