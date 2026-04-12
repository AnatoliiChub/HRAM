import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlinComposeCompiler)
}

kotlin {
    jvm()

    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(project(":app-di"))
                // Provides Dispatchers.Main for Compose Desktop (Swing event loop)
                implementation(libs.kotlinx.coroutines.swing)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.achub.hram.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg)
            packageName = "HRAM"
            packageVersion = "1.0.0"
            description = "Heart Rate Activity Monitoring"
            macOS {
                iconFile.set(project.file("icons/hram.icns"))
                bundleID = "com.achub.hram"
                entitlementsFile.set(project.file("macos/entitlements.plist"))
                infoPlist {
                    extraKeysRawXml = project.file("macos/info-plist-extra.xml").readText()
                }
            }
        }
    }
}
