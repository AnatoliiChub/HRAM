plugins {
    alias(libs.plugins.kmp.convention)
    alias(libs.plugins.cmp.convention)
    alias(libs.plugins.quality.convention)
}

extra["koverIncludes"] = listOf("com.achub.hram.view.**", "com.achub.hram.style.**")

kotlin {
    android {
        namespace = "com.achub.hram.ui.lib"

        androidResources {
            enable = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
