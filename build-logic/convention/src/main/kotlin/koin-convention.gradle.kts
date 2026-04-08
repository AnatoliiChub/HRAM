plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.google.devtools.ksp")
}

val catalog = versionCatalogs.named("libs")

// --- KSP: Koin generated sources ---
kotlin {
    sourceSets.named("commonMain").configure {
        kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
    }
}

// --- Koin dependencies ---
val koinCore = catalog.findLibrary("koin.core").get()
val koinAnnotations = catalog.findLibrary("koin.annotations").get()
val koinAndroid = catalog.findLibrary("koin.android").get()
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(koinCore)
            api(koinAnnotations)
        }
        androidMain.dependencies {
            implementation(koinAndroid)
        }
    }
}

// --- KSP: Task ordering ---
tasks.matching { it.name.startsWith("ksp") && it.name != "kspCommonMainKotlinMetadata" }.configureEach {
    dependsOn("kspCommonMainKotlinMetadata")
}

// --- KSP: Koin compiler dependencies ---
val koinKspCompiler = catalog.findLibrary("koin.ksp.compiler").get()
dependencies {
    add("kspCommonMainMetadata", koinKspCompiler)
    add("kspAndroid", koinKspCompiler)
    add("kspIosArm64", koinKspCompiler)
    add("kspIosSimulatorArm64", koinKspCompiler)
}

// --- KSP: Koin args ---
ksp {
    arg("KOIN_LOG_TIMES", "true")
    arg("KOIN_CONFIG_CHECK", "true")
}

