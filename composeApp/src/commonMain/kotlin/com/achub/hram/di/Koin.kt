package com.achub.hram.di

import com.achub.hram.screen.activities.ActivitiesViewModel
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.ksp.generated.module
import org.koin.mp.KoinPlatform

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin { modules((AppModule().module)) }
    KoinPlatform.getKoin().get<ActivitiesViewModel>()
}
