plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("dev.mokkery")
    id("org.jetbrains.kotlin.plugin.allopen")
}

// OpenForMokkery is compiled once in the :annotations module.
// compileOnly makes it available at compile time without being included in
// this module's output AAR, preventing duplicate-class conflicts at DEX merge.
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
