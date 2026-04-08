plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.google.devtools.ksp")
    id("androidx.room")
}

val catalog = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(catalog.findLibrary("androidx-room-runtime").get())
            implementation(catalog.findLibrary("androidx-sqlite-bundled").get())
        }
    }
}

dependencies {
    val roomCompiler = catalog.findLibrary("androidx-room-compiler").get()
    add("kspAndroid", roomCompiler)
    add("kspIosSimulatorArm64", roomCompiler)
    add("kspIosArm64", roomCompiler)
    add("kspJvm", roomCompiler)
}

room {
    schemaDirectory("$projectDir/schemas")
}
