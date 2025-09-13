package com.achub.hram

import kotlin.math.round

/**
 * round numbers to 2 decimal places and format as string
 * Temporary fix for https://youtrack.jetbrains.com/issue/KT-21644
 */
fun Float.format(): String {
    val rounded = round(this * 100) / 100
    val parts = rounded.toString().split(".")
    val intPart = parts[0]
    val fracPart = parts.getOrElse(1) { "0" }.padEnd(2, '0').take(2)
    return "$intPart.$fracPart"
}
