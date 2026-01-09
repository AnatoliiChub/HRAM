package com.achub.hram.utils

import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
import kotlinx.datetime.format.char

val dateFormat = LocalTime.Format {
    hour()
    char(':')
    minute()
    char(':')
    second()
}

fun formatElapsedTime(elapsedTimeSeconds: Long): String {
    val time = LocalTime.fromSecondOfDay(elapsedTimeSeconds.toInt())
    return time.format(dateFormat)
}
