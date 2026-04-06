plugins {
    alias(libs.plugins.kmp.convention)
    alias(libs.plugins.koin.convention)
    alias(libs.plugins.room.convention)
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    android {
        namespace = "com.achub.hram.data"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":domain"))
            implementation(project(":ble"))

            // DataStore
            implementation(libs.datastore)
            implementation(libs.datastore.preferences)

            // Serialization + okio
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.okio)
        }
    }
}


