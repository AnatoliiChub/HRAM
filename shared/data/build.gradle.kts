plugins {
    id("kmp-library-convention")
    id("koin-convention")
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.androidx.room)
}

kotlin {
    android {
        namespace = "com.achub.hram.data"
    }

    sourceSets {
        commonMain.dependencies {
            // Domain interfaces + models
            api(project(":domain"))

            // BLE — only data layer depends on BLE
            implementation(project(":ble"))

            // Room
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)

            // DataStore
            implementation(libs.datastore)
            implementation(libs.datastore.preferences)

            // Serialization + okio
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.okio)
        }
    }
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
}

room {
    schemaDirectory("$projectDir/schemas")
}

