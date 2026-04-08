package com.achub.hram.ext

import androidx.compose.runtime.Composable
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory

@Composable
actual fun permissionController(): BlePermissionController {
    val permissionsFactory = rememberPermissionsControllerFactory()
    val controller = permissionsFactory.createPermissionsController()
    BindEffect(controller)
    return MokoBlePermissionController(controller)
}
