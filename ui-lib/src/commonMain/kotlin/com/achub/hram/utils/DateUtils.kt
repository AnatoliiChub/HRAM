package com.achub.hram.utils

import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
import kotlinx.datetime.format.char

private val dateFormat = LocalTime.Format {
    hour()
    char(':')
    minute()
    char(':')
    second()
}

fun formatElapsedTime(timeInMs: Long): String {
    val time = LocalTime.fromMillisecondOfDay(timeInMs.toInt())
    return time.format(dateFormat)
}
