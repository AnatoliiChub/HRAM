plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

val catalog = versionCatalogs.named("libs")

val foundation = catalog.findLibrary("foundation").get()
val material3 = catalog.findLibrary("material3").get()
val ui = catalog.findLibrary("ui").get()
val uiToolingPreview = catalog.findLibrary("ui-tooling-preview").get()
val uiTooling = catalog.findLibrary("androidx-ui-tooling").get()
val componentsResources = catalog.findLibrary("components-resources").get()
val lifecycleRuntimeCompose = catalog.findLibrary("androidx-lifecycle-runtimeCompose").get()

kotlin {
    sourceSets {
        androidMain {
            dependencies {
                implementation(uiTooling)
            }
        }

        commonMain.dependencies {
            implementation(foundation)
            implementation(material3)
            implementation(ui)
            implementation(uiToolingPreview)
            implementation(componentsResources)
            implementation(lifecycleRuntimeCompose)
        }
    }
}
