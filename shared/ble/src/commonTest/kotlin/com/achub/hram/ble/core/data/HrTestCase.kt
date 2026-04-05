package com.achub.hram.ble.core.data

data class HrTestCase(
    val name: String,
    val data: ByteArray,
    val expectedHr: Int,
    val expectedSupported: Boolean,
    val expectedContact: Boolean
)
