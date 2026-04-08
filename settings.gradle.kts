rootProject.name = "HRAM"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":presentation", ":androidApp", ":ble", ":ui-lib", ":annotations", ":logger", ":data", ":domain", ":app-di")
project(":logger").projectDir = file("shared/libs/logger")
project(":presentation").projectDir = file("shared/presentation")
project(":ble").projectDir = file("shared/libs/ble")
project(":ui-lib").projectDir = file("shared/libs/ui-lib")
project(":annotations").projectDir = file("shared/libs/annotations")
project(":data").projectDir = file("shared/data")
project(":domain").projectDir = file("shared/domain")
project(":app-di").projectDir = file("shared/app-di")
