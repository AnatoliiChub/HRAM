import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("io.gitlab.arturbosch.detekt")
    id("dev.mokkery")
    id("org.jetbrains.kotlinx.kover")
    id("org.jetbrains.kotlin.plugin.allopen")
    id("com.google.devtools.ksp")
}

val catalog = versionCatalogs.named("libs")

/** Extension: set kover include patterns, e.g. `extra["koverIncludes"] = listOf("com.achub.hram.ble.**")` */
extra["koverIncludes"] = emptyList<String>()

// --- allOpen ---
allOpen {
    annotation("com.achub.hram.ble.OpenForMokkery")
}

// --- Kover ---
afterEvaluate {
    @Suppress("UNCHECKED_CAST")
    val includesPatterns = extra["koverIncludes"] as List<String>
    kover {
        reports {
            filters {
                includes {
                    includesPatterns.forEach { classes(it) }
                }
                excludes {
                    classes("*.models.*")
                    classes("*Android*")
                    classes("*Ios*")
                }
            }
        }
    }
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
kotlin {
    androidLibrary {
        compileSdk = catalog.findVersion("android-compileSdk").get().requiredVersion.toInt()
        minSdk = catalog.findVersion("android-minSdk").get().requiredVersion.toInt()

        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }

        compilations.all {
            compileTaskProvider.configure {
                compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
            }
        }

        withHostTest {}
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    )

    // --- KSP: Koin generated sources ---
    sourceSets.named("commonMain").configure {
        kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
    }

    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

// --- Mokkery ---
mokkery {
    ignoreInlineMembers.set(true)
    ignoreFinalMembers.set(true)
}

// --- Detekt ---
detekt {
    toolVersion = catalog.findVersion("detekt").get().requiredVersion
    allRules = false
    parallel = true
    source.setFrom(
        files(
            "src/commonMain/kotlin",
            "src/androidMain/kotlin",
            "src/iosMain/kotlin"
        )
    )
    config.setFrom(files("$rootDir/detekt/detekt.yml"))
}

tasks.withType<Detekt>().configureEach {
    reports {
        html.required.set(true)
        html.outputLocation.set(file("${projectDir.path}/build/reports/detekt.html"))
        xml.required.set(false)
        txt.required.set(false)
        sarif.required.set(false)
        md.required.set(false)
    }
}

// --- KSP: Koin compiler dependencies ---
val koinKspCompiler = catalog.findLibrary("koin.ksp.compiler").get()
dependencies {
    add("kspCommonMainMetadata", koinKspCompiler)
    add("kspAndroid", koinKspCompiler)
    add("kspIosArm64", koinKspCompiler)
    add("kspIosSimulatorArm64", koinKspCompiler)
}

// --- KSP: Task ordering ---
tasks.matching { it.name.startsWith("ksp") && it.name != "kspCommonMainKotlinMetadata" }.configureEach {
    dependsOn("kspCommonMainKotlinMetadata")
}

// --- KSP: Koin args ---
ksp {
    arg("KOIN_LOG_TIMES", "true")
    arg("KOIN_CONFIG_CHECK", "true")
}
