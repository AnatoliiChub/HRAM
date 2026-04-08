package com.achub.hram.ext

import androidx.compose.runtime.Composable

@Suppress("ComposableNaming")
@Composable
expect fun requestBluetooth()

@Composable
expect fun permissionController(): BlePermissionController
