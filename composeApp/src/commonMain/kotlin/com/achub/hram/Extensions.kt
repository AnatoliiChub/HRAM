package com.achub.hram

import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
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

context(screenModel: ViewModel)
fun <T> MutableStateFlow<T>.stateInExt(
    initialValue: T
) = stateIn(
    scope = screenModel.viewModelScope,
    started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000),
    initialValue = initialValue
)

@Composable
fun permissionController(): PermissionsController {
    val permissionsFactory = rememberPermissionsControllerFactory()
    val controller = permissionsFactory.createPermissionsController()
    BindEffect(controller)
    return controller
}

fun smoothOut(): ExitTransition = shrinkOut(tween(300, easing = LinearEasing)) + fadeOut()

expect fun currentThread(): String

fun  <T> Flow<T>.launchIn(
    scope: CoroutineScope,
    context: CoroutineContext
) = scope.launch(context) { collect() }

