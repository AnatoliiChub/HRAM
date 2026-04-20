package com.achub.hram.models

import kotlinx.serialization.Serializable

@Serializable
enum class BiologicalSex {
    Male, Female, Other,
}

@Serializable
data class UserSettings(
    val birthYear: Int,
    val biologicalSex: BiologicalSex,
    val weightKg: Float,
    val heightCm: Int,
) {
    companion object {
        val Default = UserSettings(
            birthYear = 1990,
            biologicalSex = BiologicalSex.Male,
            weightKg = 75f,
            heightCm = 175
        )
    }
}
