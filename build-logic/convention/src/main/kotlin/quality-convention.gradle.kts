import io.gitlab.arturbosch.detekt.Detekt

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("io.gitlab.arturbosch.detekt")
    id("org.jetbrains.kotlinx.kover")
}

val catalog = versionCatalogs.named("libs")

// Extension: set kover include patterns, e.g. `extra["koverIncludes"] = listOf("com.achub.hram.ble.**")` */
extra["koverIncludes"] = emptyList<String>()

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
