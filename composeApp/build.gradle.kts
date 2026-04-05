@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("kmp-library-convention")
    id("cmp-ui-lib-convention")
    id("koin-convention")
    id("quality-convention")
    id("test-mocking-convention")
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.androidx.room)
}

extra["koverIncludes"] = listOf("com.achub.hram.utils.**")

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

            // Lifecycle + ViewModel
            implementation(libs.koin.compose.viewmodel)

            // BLE
            implementation(project(":ble"))

            // Permission
            implementation(libs.moko.compose)
            implementation(libs.moko.main)
            implementation(libs.moko.ble)

            // Data store
            implementation(libs.datastore)
            implementation(libs.datastore.preferences)
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)

            // Serialization
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.okio)
            implementation(libs.kotlinx.datetime)

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

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
}

room {
    schemaDirectory("$projectDir/schemas")
}

tasks {
    configureEach {
        if (this.name.contains("kspDebugKotlinAndroid")) {
            this.dependsOn("generateResourceAccessorsForAndroidMain")
            this.dependsOn("generateResourceAccessorsForAndroidDebug")
            this.dependsOn("generateActualResourceCollectorsForAndroidMain")
            this.dependsOn("generateComposeResClass")
            this.dependsOn("generateExpectResourceCollectorsForCommonMain")
            this.dependsOn("kspCommonMainKotlinMetadata")
        }
        if (this.name.contains("kspKotlinIos")) {
            this.dependsOn("kspCommonMainKotlinMetadata")
        }
    }
}
