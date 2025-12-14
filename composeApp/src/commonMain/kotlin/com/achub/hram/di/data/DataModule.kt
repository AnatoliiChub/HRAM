package com.achub.hram.di.data

import com.achub.hram.data.repo.HrActivityRepo
import com.achub.hram.data.repo.HramHrActivityRepo
import com.achub.hram.data.db.HramDatabase
import com.achub.hram.data.db.dao.ActivityDao
import com.achub.hram.data.db.dao.HeartRateDao
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Module(includes = [DatabaseModule::class])
@Configuration
class DataModule {
    @Single
    fun provideHeartRateDao(
        @Provided dataBase: HramDatabase
    ) = dataBase.getHeartRateDao()

    @Single
    fun provideActivityDao(
        @Provided database: HramDatabase
    ) = database.getActivityDao()

    @Single
    fun provideHrActivityRepo(actDao: ActivityDao, hrDao: HeartRateDao): HrActivityRepo =
        HramHrActivityRepo(actDao, hrDao)
}
