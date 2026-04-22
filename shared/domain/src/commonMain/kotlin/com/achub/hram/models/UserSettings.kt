package com.achub.hram.models

import kotlinx.serialization.Serializable

@Serializable
enum class BiologicalSex {
    Male, Female, Other,
}

@Serializable
enum class AppTheme {
    System, Dark, Light
}

@Serializable
data class UserSettings(
    val birthYear: Int,
    val biologicalSex: BiologicalSex,
    val weightKg: Float,
    val heightCm: Int,
    val theme: AppTheme = AppTheme.System,
) {
    companion object {
        val Default = UserSettings(
            birthYear = 1990,
            biologicalSex = BiologicalSex.Male,
            weightKg = 75f,
            heightCm = 175,
            theme = AppTheme.System
        )
    }
}
