import io.gitlab.arturbosch.detekt.Detekt

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("io.gitlab.arturbosch.detekt")
    id("org.jetbrains.kotlinx.kover")
}

val catalog = versionCatalogs.named("libs")

// Extension: set kover include/exclude patterns, e.g.
// `extra["koverIncludes"] = listOf("com.achub.hram.ble.**")`
// `extra["koverExcludes"] = listOf("com.achub.hram.ble.BleFactory")`

extra["koverExcludes"] = emptyList<String>()
// --- Kover ---
afterEvaluate {
    @Suppress("UNCHECKED_CAST")
    kover {
        reports {
            @Suppress("UNCHECKED_CAST")
            val excludesPatterns = (extra["koverExcludes"] as? List<String>) ?: emptyList()
            val includesPatterns = (extra["koverIncludes"] as? List<String>) ?: emptyList()
            filters {
                includes {
                    includesPatterns.forEach { classes(it) }
                }
                excludes {
                    classes("*.models.*")
                    excludesPatterns.forEach { classes(it) }
                }
                // Project-wide sensible defaults
            }
        }
    }
    // Project-specific excludes from build.gradle (e.g. to exclude factory utilities)
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
        sarif.required.set(false)
        md.required.set(false)
    }
}
