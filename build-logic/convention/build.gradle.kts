plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.plugins.kotlinMultiplatform.toProvider())
    implementation(libs.plugins.android.kotlin.multiplatform.library.toProvider())
    implementation(libs.plugins.detekt.toProvider())
    implementation(libs.plugins.mokkery.toProvider())
    implementation(libs.plugins.kover.toProvider())
    implementation(libs.plugins.kotlin.allopen.toProvider())
    implementation(libs.plugins.ksp.toProvider())
}

private fun Provider<PluginDependency>.toProvider(): Provider<String> =
    map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }
