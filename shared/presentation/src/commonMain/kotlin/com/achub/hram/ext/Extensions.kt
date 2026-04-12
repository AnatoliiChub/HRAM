package com.achub.hram.ext

import kotlin.math.round

private const val DECIMAL_MULTIPLIER = 100
private const val PAD_END_LENGTH = 2

/**
 * round numbers to 2 decimal places and format as string
 * Temporary fix for https://youtrack.jetbrains.com/issue/KT-21644
 */
fun Float.format(): String {
    val rounded = round(this * DECIMAL_MULTIPLIER) / DECIMAL_MULTIPLIER
    val parts = rounded.toString().split(".")
    val intPart = parts[0]
    val fracPart = parts.getOrElse(1) { "0" }.padEnd(PAD_END_LENGTH, '0').take(PAD_END_LENGTH)
    return "$intPart.$fracPart"
}

expect fun currentThread(): String
