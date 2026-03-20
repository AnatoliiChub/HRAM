plugins {
    id("kmp-library-convention")
    alias(libs.plugins.kotlinxSerialization)
}

extra["koverIncludes"] = listOf("com.achub.hram.ble.**")

kotlin {
    androidLibrary {
        namespace = "com.achub.hram.ble"
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
        }
        commonMain.dependencies {
            // Coroutines
            implementation(libs.runtime)

            // BLE
            api(libs.kable)

            // Logging
            implementation(libs.logger)

            // DI
            implementation(libs.koin.core)
            api(libs.koin.annotations)

            // Serialization
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

