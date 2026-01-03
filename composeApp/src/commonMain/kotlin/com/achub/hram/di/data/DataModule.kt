package com.achub.hram.di.data

import androidx.datastore.core.DataStore
import com.achub.hram.data.db.HramDatabase
import com.achub.hram.data.db.dao.ActivityDao
import com.achub.hram.data.db.dao.HeartRateDao
import com.achub.hram.data.models.BleState
import com.achub.hram.data.repo.HrActivityRepo
import com.achub.hram.data.repo.HramHrActivityRepo
import com.achub.hram.data.repo.HramTrackingStateRepo
import com.achub.hram.data.repo.TrackingStateRepo
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Module(includes = [DatabaseModule::class, DataStoreModule::class])
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

    @Single
    fun provideTrackingStateRepo(datastore: DataStore<BleState>): TrackingStateRepo =
        HramTrackingStateRepo(datastore)
}
