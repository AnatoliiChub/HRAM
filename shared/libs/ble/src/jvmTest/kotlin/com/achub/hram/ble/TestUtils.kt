package com.achub.hram.ble

import com.juul.kable.Identifier
import com.juul.kable.toIdentifier

actual fun identifier(id: String): Identifier {
    val hash = id.encodeToByteArray().fold(0L) { acc, b -> acc * 31 + b.toLong() }
    val hex = (hash and 0x0000_FFFF_FFFF_FFFFL).toString(16).padStart(12, '0')
    return "00000000-0000-4000-8000-$hex".toIdentifier()
}
