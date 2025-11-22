package com.achub.hram

import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.achub.hram.data.db.entity.ActivityEntity
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.bluetooth.BLUETOOTH_CONNECT
import dev.icerock.moko.permissions.bluetooth.BLUETOOTH_SCAN
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.round
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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

context(viewModel: ViewModel)
fun <T> MutableStateFlow<T>.stateInExt(
    initialValue: T
) = stateIn(
    scope = viewModel.viewModelScope,
    started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000),
    initialValue = initialValue
)

context(viewModel: ViewModel)
fun <T> Flow<T>.stateInExt(
    initialValue: T
) = stateIn(
    scope = viewModel.viewModelScope,
    started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000),
    initialValue = initialValue
)

fun logger(tag: String?, message: () -> String) {
    Napier.d { "[$tag] ${message()}" }
}

fun loggerE(tag: String?, message: () -> String) {
    Napier.e { "[$tag] ${message()}" }
}

@Composable
fun permissionController(): PermissionsController {
    val permissionsFactory = rememberPermissionsControllerFactory()
    val controller = permissionsFactory.createPermissionsController()
    BindEffect(controller)
    return controller
}

fun smoothOut(): ExitTransition = shrinkVertically(tween(300, easing = LinearEasing)) + fadeOut()

expect fun currentThread(): String

fun <T> Flow<T>.launchIn(
    scope: CoroutineScope,
) = scope.launch { collect() }

fun tickerFlow(period: Duration, initialDelay: Duration = Duration.ZERO) = flow {
    delay(initialDelay)
    while (true) {
        emit(Unit)
        delay(period)
    }
}

suspend fun PermissionsController.requestBleBefore(action: () -> Unit, onFailure: () -> Unit) {
    try {
        providePermission(Permission.BLUETOOTH_SCAN)
        providePermission(Permission.BLUETOOTH_CONNECT)
        action()
    } catch (exception: Exception) {
        loggerE("PermissionsController") { "requestBlePermissionBeforeAction Error : $exception" }
        onFailure()
    }
}

fun MutableList<Job>.cancelAndClear() {
    this.forEach { it.cancel() }
    this.clear()
}


@OptIn(ExperimentalUuidApi::class)
fun createActivity(name: String, currentTime: Long): ActivityEntity {
    val activity = ActivityEntity(
        Uuid.Companion.random().toString() + currentTime,
        name,
        0L,
        currentTime
    )
    return activity
}

@OptIn(ExperimentalTime::class)
fun Long.fromEpochSeconds() = Instant.fromEpochSeconds(this).toLocalDateTime(TimeZone.currentSystemDefault())

@Composable
fun Dp.dpToPx() = with(LocalDensity.current) { this@dpToPx.toPx() }

@Composable
fun Int.pxToDp() = with(LocalDensity.current) { this@pxToDp.toDp() }

fun formatTime(seconds: Long): String {
    fun formatSixty(seconds: Long) = "${if (seconds < 10) "0" else ""}${seconds}"
    return when {
        seconds < 60L -> "$seconds s"
        seconds < 3600L -> "${seconds / 60}:${formatSixty(seconds % 60)}"
        else -> "${seconds / 3600}:${formatSixty(seconds / 60 % 60)}:${formatSixty(seconds % 60)}"
    }
}
