package com.achub.hram.ble

import com.juul.kable.Identifier
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
actual fun identifier(id: String): Identifier = Identifier.fromByteArray(id.encodeToByteArray())
