package com.achub.hram.ble

/**
 * Duration for each Bluetooth Low Energy (BLE) scan cycle, in milliseconds.
 */
const val BLE_SCAN_DURATION = 5000L

/**
 * TTL for cached BLE advertisements, in milliseconds.
 * Cached entries are reused in [connectToDevice] to avoid triggering a new BLE scan
 * immediately after [scanHrDevices] has already found the device.
 */
const val ADVERTISEMENT_CACHE_TTL_MS = 5_000L

