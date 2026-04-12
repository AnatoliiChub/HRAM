@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
    applyDefaultHierarchyTemplate {
        common {
            group("mobile") {
                withAndroidTarget()
                withIos()
            }
            withJvm()
        }
    }

    sourceSets {
        // Explicit dependsOn wires the metadata compilations (compileAndroidMain,
        // compileIosMainKotlinMetadata) so they can see actual declarations in mobileMain.
        val mobileMain by getting
        androidMain.get().dependsOn(mobileMain)
        iosMain.get().dependsOn(mobileMain)
    }
}
