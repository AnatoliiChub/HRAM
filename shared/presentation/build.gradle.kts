@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.kmp.convention)
    alias(libs.plugins.cmp.convention)
    alias(libs.plugins.koin.convention)
    alias(libs.plugins.quality.convention)
    alias(libs.plugins.test.mocking.convention)
}

// Keep the generated resources package name stable (was composeApp, now presentation)
compose.resources { packageOfResClass = "hram.composeapp.generated.resources" }

// presentation is not the DI root — disable Koin config check
ksp { arg("KOIN_CONFIG_CHECK", "false") }

kotlin {
    // Register "mobileMain" as a proper intermediate source set covering Android + iOS.
    // The IDE reads this template to understand source-set scopes — this is what silences
    // the "conflicting overloads" false-positive on actual fun permissionController().
    applyDefaultHierarchyTemplate {
        common {
            group("mobile") { // → creates mobileMain wired to androidMain + iosMain
                withAndroidTarget()
                withIos()
            }
            withJvm() // jvmMain sits directly under common (no intermediate group)
        }
    }

    android {
        namespace = "com.achub.hram.library"

        packaging {
            resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
        }

        androidResources { enable = true }
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
        // applyDefaultHierarchyTemplate wires leaf targets → mobileMain for platform
        // compilations, but the metadata compilation tasks (compileAndroidMain,
        // compileIosMainKotlinMetadata) also need explicit dependsOn to see mobileMain symbols.
        val mobileMain by getting
        androidMain.get().dependsOn(mobileMain)
        iosMain.get().dependsOn(mobileMain)

        mobileMain.dependencies {
            implementation(libs.moko.compose)
            implementation(libs.moko.main)
            implementation(libs.moko.ble)
        }

        commonMain.dependencies {
            implementation(project(":ui-lib"))
            api(project(":domain"))
            implementation(libs.koin.compose.viewmodel)
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

