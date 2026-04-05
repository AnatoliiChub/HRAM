plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("dev.mokkery")
    id("org.jetbrains.kotlin.plugin.allopen")
}

/**
 * Annotation FQNs whose classes should be opened for Mokkery mocking.
 * Set per-module before the plugin runs, e.g.:
 *   extra["mokkeryAnnotations"] = listOf("com.achub.hram.ble.OpenForMokkery")
 */
extra["mokkeryAnnotations"] = emptyList<String>()

// --- allOpen ---
afterEvaluate {
    @Suppress("UNCHECKED_CAST")
    val annotations = extra["mokkeryAnnotations"] as List<String>
    allOpen {
        annotations.forEach { annotation(it) }
    }
}

// --- Mokkery ---
mokkery {
    ignoreInlineMembers.set(true)
    ignoreFinalMembers.set(true)
}

