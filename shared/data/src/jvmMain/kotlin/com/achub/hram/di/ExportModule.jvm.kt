package com.achub.hram.di

import com.achub.hram.export.DesktopFileExporter
import com.achub.hram.export.FileExporter
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.core.scope.Scope

@Module
@Configuration
actual class ExportModule actual constructor() {
    @Single
    actual fun provideFileExporter(scope: Scope): FileExporter = DesktopFileExporter()
}
