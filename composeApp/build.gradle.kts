@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("kmp-library-convention")
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlinComposeCompiler)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.androidx.room)
}

extra["koverIncludes"] = listOf("com.achub.hram.utils.**")

kotlin {
    androidLibrary {
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
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
            implementation(libs.androidx.ui.tooling)
        }
        commonMain.dependencies {
            implementation(libs.runtime)
            implementation(libs.foundation)
            implementation(libs.material3)
            implementation(libs.ui)
            implementation(libs.ui.tooling.preview)
            implementation(libs.components.resources)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.logger)
            implementation(libs.kotlinx.datetime)

            // Dependency Injection
            implementation(libs.koin.core)
            implementation(libs.koin.compose.viewmodel)
            api(libs.koin.annotations)

            // BLE
            api(project(":ble"))

            // Permission
            api(libs.moko.compose)
            api(libs.moko.main)
            api(libs.moko.ble)

            // Data store
            implementation(libs.datastore)
            implementation(libs.datastore.preferences)
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)

            // Serialization
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.okio)
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

