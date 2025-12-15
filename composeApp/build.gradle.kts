@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlinComposeCompiler)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.detekt)
    alias(libs.plugins.mokkery)
    alias(libs.plugins.kover)
    alias(libs.plugins.kotlin.allopen)
}

mokkery {
    ignoreInlineMembers.set(true) // ignores only inline members
    ignoreFinalMembers.set(true) // ignores final members (inline included)
}

allOpen {
    annotation("com.achub.hram.OpenForMokkery")
}

detekt {
    toolVersion = libs.versions.detekt.get()
    allRules = false
    parallel = true
    source.setFrom(
        files(
            "src/commonMain/kotlin",
            "src/androidMain/kotlin",
            "src/iosMain/kotlin"
        )
    )
    config.setFrom(files("$rootDir/detekt/detekt.yml"))
}

kover {
    reports {
        filters {
            includes {
                classes("com.achub.hram.ble.*")
                classes("com.achub.hram.tracking.*")
                classes("com.achub.hram.utils.*")
            }
            excludes {
                classes("*.models.*")
                classes("*Android*")
                classes("*Ios*")
            }
        }
    }
}

kotlin {
    androidTarget {
        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
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
            // Required when using NativeSQLiteDriver
            linkerOpts.add("-lsqlite3")
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
            implementation(libs.kable)

            // Permission
            api(libs.moko.compose)
            api(libs.moko.main)
            api(libs.moko.ble)

            // Data store
            implementation(libs.datastore)
            implementation(libs.datastore.preferences)
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
    // KSP Common sourceSet
    sourceSets.named("commonMain").configure {
        kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
    }

    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

dependencies {
    add("kspCommonMainMetadata", libs.koin.ksp.compiler)
    add("kspAndroid", libs.koin.ksp.compiler)
    add("kspIosArm64", libs.koin.ksp.compiler)
    add("kspIosSimulatorArm64", libs.koin.ksp.compiler)
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    debugImplementation(libs.ui.tooling.preview)
}

room {
    schemaDirectory("$projectDir/schemas")
}

tasks.withType<Detekt>().configureEach {
    reports {
        html.required.set(true)
        html.outputLocation.set(file("${projectDir.path}/build/reports/detekt.html"))
        xml.required.set(false)
        txt.required.set(false)
        sarif.required.set(false)
        md.required.set(false)
    }
}

// KSP Metadata Trigger
tasks.matching { it.name.startsWith("ksp") && it.name != "kspCommonMainKotlinMetadata" }.configureEach {
    dependsOn("kspCommonMainKotlinMetadata")
}

tasks {
    configureEach {
        if (this.name.contains("kspDebugKotlinAndroid")) {
            this.dependsOn("generateResourceAccessorsForAndroidMain")
            this.dependsOn("generateResourceAccessorsForAndroidDebug")
            this.dependsOn("generateActualResourceCollectorsForAndroidMain")
            this.dependsOn("generateComposeResClass")
            this.dependsOn("generateExpectResourceCollectorsForCommonMain")
            this.dependsOn("generateActualResourceCollectorsForAndroidMain")
            this.dependsOn("kspCommonMainKotlinMetadata")
        }
        if (this.name.contains("kspKotlinIos")) {
            // Ensure it depends on the metadata task that contains all common metadata for KSP to work.
            this.dependsOn("kspCommonMainKotlinMetadata")
        }
    }
}

ksp {
    arg("KOIN_LOG_TIMES", "true")
    arg("KOIN_CONFIG_CHECK", "true")
}

android {
    namespace = "com.achub.hram"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "com.achub.hram"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
