plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("dev.mokkery")
    id("org.jetbrains.kotlin.plugin.allopen")
}

// OpenForMokkery is compiled once in the :annotations module.
// Exposed as api so it is available at compile time on all platforms,
// including Kotlin/Native where compileOnly is not supported.
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":annotations"))
            }
        }
    }
}

// --- allOpen ---
allOpen {
    annotation("com.achub.hram.OpenForMokkery")
}

// --- Mokkery ---
mokkery {
    ignoreInlineMembers.set(true)
    ignoreFinalMembers.set(true)
}
