package com.achub.hram.di

import com.achub.hram.data.BleConnectionRepo
import com.achub.hram.data.BleHrDataRepo
import com.achub.hram.screen.activities.ActivitiesViewModel
import com.achub.hram.screen.record.RecordViewModel
import dev.icerock.moko.permissions.PermissionsController
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.Module

@Module
@ComponentScan
class ViewModelModule {
    @Factory
    fun provideRecordViewModel(
        bleConnectionRepo: BleConnectionRepo,
        bleHrDataRepo: BleHrDataRepo,
        @InjectedParam permissionController: PermissionsController
    ): RecordViewModel = RecordViewModel(
        bleConnectionRepo, bleHrDataRepo, permissionController
    )

    @Factory
    fun provideActivitiesViewModel(): ActivitiesViewModel = ActivitiesViewModel()
}
