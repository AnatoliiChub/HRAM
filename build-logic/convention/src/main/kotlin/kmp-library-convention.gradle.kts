import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("com.google.devtools.ksp")
}

val catalog = versionCatalogs.named("libs")

@OptIn(ExperimentalKotlinGradlePluginApi::class)
kotlin {
    android {
        compileSdk = catalog.findVersion("android-compileSdk").get().requiredVersion.toInt()
        minSdk = catalog.findVersion("android-minSdk").get().requiredVersion.toInt()

        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }

        compilations.all {
            compileTaskProvider.configure {
                compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
            }
        }

        withHostTest {}
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    )

    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}
