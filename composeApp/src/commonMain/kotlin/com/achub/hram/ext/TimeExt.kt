package com.achub.hram.ext

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private const val SECONDS_IN_MINUTE = 60L
private const val SECONDS_IN_HOUR = 3600L
private const val LEADING_ZERO_THRESHOLD = 10

fun formatTime(seconds: Long): String {
    fun formatSixty(seconds: Long) = "${if (seconds < LEADING_ZERO_THRESHOLD) "0" else ""}$seconds"
    return when {
        seconds < SECONDS_IN_MINUTE -> "$seconds s"

        seconds < SECONDS_IN_HOUR -> "${seconds / SECONDS_IN_MINUTE}:${formatSixty(seconds % SECONDS_IN_MINUTE)}"

        else -> "${seconds / SECONDS_IN_HOUR}:${formatSixty(seconds / SECONDS_IN_MINUTE % SECONDS_IN_MINUTE)}:" +
            formatSixty(seconds % SECONDS_IN_MINUTE)
    }
}

@OptIn(ExperimentalTime::class)
fun Long.fromEpochSeconds() = Instant.fromEpochSeconds(this).toLocalDateTime(TimeZone.currentSystemDefault())
