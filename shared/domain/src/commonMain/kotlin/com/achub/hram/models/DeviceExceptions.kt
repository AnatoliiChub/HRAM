package com.achub.hram.models

/**
 * Thrown when the Bluetooth adapter is unavailable (powered off or not present).
 * Data layer translates BLE-specific exceptions into this domain exception.
 */
class DeviceUnavailableException(message: String? = null) : Exception(message)

/**
 * Duration for each BLE scan cycle, in milliseconds.
 */
const val SCAN_DURATION_MS = 5000L
