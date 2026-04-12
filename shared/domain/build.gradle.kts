plugins {
    alias(libs.plugins.kmp.convention)
    alias(libs.plugins.koin.convention)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.quality.convention)
}

// domain is not the DI root — not all dependencies are defined here
ksp {
    arg("KOIN_CONFIG_CHECK", "false")
}

kotlin {
    android {
        namespace = "com.achub.hram.domain"
    }

    sourceSets {
        commonMain.dependencies {
            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
            // Serialization (BleState, TrackingStateStage, domain models @Serializable)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
