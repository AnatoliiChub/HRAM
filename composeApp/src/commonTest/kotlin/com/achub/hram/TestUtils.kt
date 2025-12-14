package com.achub.hram

import com.juul.kable.Identifier

// workaround for testing to avoid call actual String.toIdentifier extension from kable lib
fun identifier(id: String): Identifier = id as Identifier
