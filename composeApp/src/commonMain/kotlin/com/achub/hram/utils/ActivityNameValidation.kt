package com.achub.hram.utils

import hram.composeapp.generated.resources.Res
import hram.composeapp.generated.resources.activity_screen_name_validation_empty
import hram.composeapp.generated.resources.activity_screen_name_validation_start_digit
import hram.composeapp.generated.resources.activity_screen_name_validation_too_long
import hram.composeapp.generated.resources.activity_screen_name_validation_too_short

private const val MIN_NAME_LENGTH = 3
private const val MAX_NAME_LENGTH = 50

class ActivityNameValidation {

    operator fun invoke(name: String) = when {
        name.isBlank() -> Res.string.activity_screen_name_validation_empty
        name.firstOrNull()?.isDigit() ?: false -> Res.string.activity_screen_name_validation_start_digit
        name.length < MIN_NAME_LENGTH -> Res.string.activity_screen_name_validation_too_short
        name.length > MAX_NAME_LENGTH -> Res.string.activity_screen_name_validation_too_long
        else -> null
    }
}
