plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("dev.mokkery")
    id("org.jetbrains.kotlin.plugin.allopen")
}

// Inject the shared OpenForMokkery annotation source into every module's commonMain.
// Compiling it per-module means it works for all KMP targets (JVM/Android and K/N/iOS).
kotlin {
    sourceSets {
        commonMain {
            kotlin.srcDir("${rootDir}/build-logic/shared-sources")
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

