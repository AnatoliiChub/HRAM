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
            // Export API dependencies so their public classes appear in the ObjC/Swift header
            export(project(":presentation"))
            export(project(":data"))
            export(project(":domain"))
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":presentation"))
            api(project(":data"))
        }
    }
}

