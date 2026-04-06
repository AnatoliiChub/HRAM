@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("kmp-library-convention")
    id("cmp-ui-lib-convention")
    id("koin-convention")
    id("quality-convention")
    id("test-mocking-convention")
    alias(libs.plugins.kotlinxSerialization)
}

extra["koverIncludes"] = listOf("com.achub.hram.utils.**")

// composeApp is a domain/UI module, not the DI root — disable Koin config check
ksp {
    arg("KOIN_CONFIG_CHECK", "false")
}

kotlin {
    android {
        namespace = "com.achub.hram.library"

        packaging {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
            }
        }

        androidResources {
            enable = true
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.compilations.getByName("main") {
            cinterops.create("LiveActivitiBridge") {
                definitionFile.set(
                    file(rootDir.absolutePath + "/iosApp/iosApp/Bridge/LiveActivityBridge.def")
                )
                includeDirs.allHeaders(rootDir.absolutePath + "/iosApp/iosApp/Bridge/")
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            // UI components
            implementation(project(":ui-lib"))

            // Domain (business logic, interfaces, models)
            api(project(":domain"))

            // Lifecycle + ViewModel
            implementation(libs.koin.compose.viewmodel)


            // Permission
            implementation(libs.moko.compose)
            implementation(libs.moko.main)
            implementation(libs.moko.ble)

            // Serialization (for BleState, TrackingStateStage @Serializable)
            implementation(libs.kotlinx.serialization.json)

            // Logging
            implementation(libs.logger)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

