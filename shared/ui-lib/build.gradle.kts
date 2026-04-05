plugins {
    id("kmp-library-convention")
    id("cmp-ui-lib-convention")
    id("quality-convention")
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
            implementation(libs.logger)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
