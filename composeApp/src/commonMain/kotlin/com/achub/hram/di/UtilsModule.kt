package com.achub.hram.di

import com.achub.hram.data.repo.HrActivityRepo
import com.achub.hram.export.FileExporter
import com.achub.hram.usecase.ExportCsvUseCase
import com.achub.hram.usecase.ActivityNameErrorMapper
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Module
@Configuration
class UtilsModule {
    @Factory
    fun provideActivityNameValidationUseCase() = ActivityNameErrorMapper()

    @Factory
    fun provideExportCsvUseCase(
        hrActivityRepo: HrActivityRepo,
        fileExporter: FileExporter,
    ) = ExportCsvUseCase(hrActivityRepo, fileExporter)
}
