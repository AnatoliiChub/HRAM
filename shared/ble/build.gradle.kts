plugins {
    id("kmp-library-convention")
    id("quality-convention")
    id("test-mocking-convention")
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
