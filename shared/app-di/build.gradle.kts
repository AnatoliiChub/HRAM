@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("kmp-library-convention")
    id("koin-convention")
}

kotlin {
    android {
        namespace = "com.achub.hram.appdi"
    }

    val sharedName = "ComposeApp"

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = sharedName
            isStatic = true
            compilerOptions {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
            linkerOpts.add("-lsqlite3")
        }
    }

    sourceSets {
        commonMain.dependencies {
            // composeApp (UI + domain) — provides all app-level Koin modules
            api(project(":composeApp"))
            // data — provides DataModule
            api(project(":data"))
            // ble — provides BleModule
            api(project(":ble"))
            // Logging (for initKoin Napier setup)
            implementation(libs.logger)
        }
    }
}

