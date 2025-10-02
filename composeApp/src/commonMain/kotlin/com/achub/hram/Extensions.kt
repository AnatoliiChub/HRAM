package com.achub.hram

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
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

context(screenModel: ScreenModel)
fun <T> MutableStateFlow<T>.stateInExt(
    initialValue: T
) = stateIn(
    scope = screenModel.screenModelScope,
    started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000),
    initialValue = initialValue
)

@Composable
fun permissionController(): PermissionsController {
    val permissionsFactory = rememberPermissionsControllerFactory()
    val controller =  permissionsFactory.createPermissionsController()
    BindEffect(controller)
    return controller
}
