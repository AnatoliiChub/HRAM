plugins {
    alias(libs.plugins.kmp.convention)
    alias(libs.plugins.quality.convention)
    alias(libs.plugins.test.mocking.convention)
    alias(libs.plugins.kotlinxSerialization)
}

extra["koverIncludes"] = listOf("com.achub.hram.ble.**")

kotlin {
    android {
        namespace = "com.achub.hram.ble"
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
        }
        commonMain.dependencies {
            implementation(libs.kable)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
