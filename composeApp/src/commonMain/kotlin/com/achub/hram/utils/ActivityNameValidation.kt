package com.achub.hram.utils

private const val MIN_NAME_LENGTH = 3
private const val MAX_NAME_LENGTH = 50

class ActivityNameValidation {

    operator fun invoke(name: String) = when {
        name.isBlank() -> "Activity name cannot be empty"
        name.firstOrNull()?.isDigit() ?: false -> "Activity name cannot start with a digit"
        name.length < MIN_NAME_LENGTH -> "Activity name is too short"
        name.length > MAX_NAME_LENGTH -> "Activity name is too long"
        else -> null
    }
}
