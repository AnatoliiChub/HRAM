package com.achub.hram.ext

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.achub.hram.data.db.entity.ActivityEntity
import com.juul.kable.UnmetRequirementException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.bluetooth.BLUETOOTH_CONNECT
import dev.icerock.moko.permissions.bluetooth.BLUETOOTH_SCAN
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.launch
import kotlin.math.round
import kotlin.time.Duration
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val DECIMAL_MULTIPLIER = 100
private const val PAD_END_LENGTH = 2
private const val BLE_OFF_MESSAGE = "Bluetooth is powered off"

/**
 * round numbers to 2 decimal places and format as string
 * Temporary fix for https://youtrack.jetbrains.com/issue/KT-21644
 */
fun Float.format(): String {
    val rounded = round(this * DECIMAL_MULTIPLIER) / DECIMAL_MULTIPLIER
    val parts = rounded.toString().split(".")
    val intPart = parts[0]
    val fracPart = parts.getOrElse(1) { "0" }.padEnd(PAD_END_LENGTH, '0').take(PAD_END_LENGTH)
    return "$intPart.$fracPart"
}

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

expect fun currentThread(): String

fun <T> Flow<T>.launchIn(scope: CoroutineScope) = scope.launch { collect() }

fun tickerFlow(period: Duration, initialDelay: Duration = Duration.ZERO) = flow {
    delay(initialDelay)
    while (true) {
        emit(Unit)
        delay(period)
    }
}

@Suppress("detekt:TooGenericExceptionCaught")
suspend fun PermissionsController.requestBleBefore(
    action: () -> Unit,
    onFailure: () -> Unit,
) {
    try {
        providePermission(Permission.BLUETOOTH_SCAN)
        providePermission(Permission.BLUETOOTH_CONNECT)
        action()
    } catch (ex: Exception) {
        loggerE("PermissionsController") { "requestBlePermissionBeforeAction Error : $ex" }
        val bleOff = (ex is DeniedException) && (ex.message == BLE_OFF_MESSAGE) || (ex is UnmetRequirementException)
        if (bleOff) return else onFailure()
    }
}

fun MutableList<Job>.cancelAndClear() {
    this.forEach { it.cancel() }
    this.clear()
}

@OptIn(ExperimentalUuidApi::class)
fun createActivity(name: String, currentTime: Long): ActivityEntity {
    val activity = ActivityEntity(
        Uuid.random().toString() + currentTime,
        name,
        0L,
        currentTime
    )
    return activity
}

@OptIn(FlowPreview::class)
fun <T> Flow<T>.cancelAfter(duration: Duration) = this.combine(
    flow {
        emit(Unit)
        delay(duration.inWholeMilliseconds + 1)
    }.timeout(duration)
) { result, _ -> result }

@Composable
fun Dp.dpToPx() = with(LocalDensity.current) { this@dpToPx.toPx() }

@Composable
fun Int.pxToDp() = with(LocalDensity.current) { this@pxToDp.toDp() }
