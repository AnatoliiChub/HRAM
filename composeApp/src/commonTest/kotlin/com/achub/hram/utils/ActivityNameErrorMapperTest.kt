package com.achub.hram.utils

import hram.composeapp.generated.resources.Res
import hram.composeapp.generated.resources.activity_screen_name_validation_empty
import hram.composeapp.generated.resources.activity_screen_name_validation_start_digit
import hram.composeapp.generated.resources.activity_screen_name_validation_too_long
import hram.composeapp.generated.resources.activity_screen_name_validation_too_short
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ActivityNameErrorMapperTest {
    private val mapper = ActivityNameErrorMapper()

    @Test
    fun `input is blank`() {
        val result = mapper("     ")
        assertEquals(Res.string.activity_screen_name_validation_empty, result)
    }

    @Test
    fun `input is less than min length`() {
        val result = mapper("ab")
        assertEquals(Res.string.activity_screen_name_validation_too_short, result)
    }

    @Test
    fun `input exceeds max length`() {
        val longName = "a".repeat(MAX_NAME_LENGTH + 1)
        val result = mapper(longName)
        assertEquals(Res.string.activity_screen_name_validation_too_long, result)
    }

    @Test
    fun `name starting with a digit`() {
        val result = mapper("3rd Morning rung")
        assertEquals(Res.string.activity_screen_name_validation_start_digit, result)
    }

    @Test
    fun `input is correct`() = assertNull(mapper("Morning Run"))
}
