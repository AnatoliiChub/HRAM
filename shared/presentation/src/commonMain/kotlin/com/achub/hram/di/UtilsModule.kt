package com.achub.hram.di

import com.achub.hram.ActivityNameErrorMapper
import com.achub.hram.data.repo.HrActivityRepo
import com.achub.hram.export.FileExporter
import com.achub.hram.tracking.BlePlatformStateHandler
import com.achub.hram.tracking.MokoBlePlatformStateHandler
import com.achub.hram.usecase.ExportCsvUseCase
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
class UtilsModule {
    @Single(binds = [BlePlatformStateHandler::class])
    fun provideBlePlatformStateHandler(): BlePlatformStateHandler = MokoBlePlatformStateHandler()

    @Factory
    fun provideActivityNameValidationUseCase() = ActivityNameErrorMapper()

    @Factory
    fun provideExportCsvUseCase(
        hrActivityRepo: HrActivityRepo,
        fileExporter: FileExporter,
    ) = ExportCsvUseCase(hrActivityRepo, fileExporter)
}
