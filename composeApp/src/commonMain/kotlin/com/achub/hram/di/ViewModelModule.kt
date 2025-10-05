package com.achub.hram.di

import com.achub.hram.screen.activities.ActivitiesViewModel
import com.achub.hram.screen.record.RecordViewModel
import org.koin.dsl.module

val viewModelModule = module {
    factory { params ->
        RecordViewModel(
            bleConnectionRepo = get(),
            bleHrDataRepo = get(),
            permissionController = params.get()
        )
    }
    factory { ActivitiesViewModel() }
}
