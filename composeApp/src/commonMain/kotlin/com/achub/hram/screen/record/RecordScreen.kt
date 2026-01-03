package com.achub.hram.screen.record

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.achub.hram.ble.models.BleDevice
import com.achub.hram.ext.permissionController
import com.achub.hram.ext.requestBluetooth
import com.achub.hram.style.Dimen16
import com.achub.hram.style.Dimen32
import com.achub.hram.style.Dimen48
import com.achub.hram.style.LabelMediumBold
import com.achub.hram.style.LabelSmall
import com.achub.hram.style.Red
import com.achub.hram.style.White
import com.achub.hram.view.components.HrButton
import com.achub.hram.view.dialogs.InfoDialog
import com.achub.hram.view.dialogs.NameActivityDialog
import com.achub.hram.view.dialogs.choosedevice.HrConnectDialog
import com.achub.hram.view.section.RecordSection
import com.achub.hram.view.section.TrackingIndicationsSection
import hram.composeapp.generated.resources.Res
import hram.composeapp.generated.resources.dialog_device_connected_message
import hram.composeapp.generated.resources.dialog_device_connected_title
import hram.composeapp.generated.resources.dialog_device_connection_failed_message
import hram.composeapp.generated.resources.dialog_device_connection_failed_title
import hram.composeapp.generated.resources.dialog_info_ok
import hram.composeapp.generated.resources.dialog_name_activity_message
import hram.composeapp.generated.resources.dialog_name_activity_title
import hram.composeapp.generated.resources.dialog_open_setting_button_text
import hram.composeapp.generated.resources.dialog_open_setting_message
import hram.composeapp.generated.resources.dialog_open_setting_title
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

const val DOUBLE_HAPTIC_INTERVAL = 150L
private const val COLUMN_SPACER_WEIGHT = 0.5f

@Composable
fun RecordScreen() {
    val controller = permissionController()
    val viewModel = koinViewModel<RecordViewModel>(parameters = { parametersOf(controller) })
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    with(viewModel) {
        if (state.requestBluetooth) {
            requestBluetooth()
            clearRequestBluetooth()
        }
        val indications = state.bleNotification
        val device = state.connectedDevice
        Box(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(Dimen16),
                horizontalAlignment = CenterHorizontally
            ) {
                Spacer(Modifier.weight(COLUMN_SPACER_WEIGHT))
                TrackingIndicationsSection(indications)
                Spacer(Modifier.size(Dimen32))
                DeviceSection(device) { viewModel.requestScanning() }
                Spacer(Modifier.weight(COLUMN_SPACER_WEIGHT))
                RecordSection(
                    recordingState = state.recordingState,
                    onPlay = ::toggleRecording,
                    onStop = ::showNameActivityDialog,
                    isRecordingEnabled = state.isRecordingEnabled
                )
            }
        }
        Dialog(
            state,
            onDeviceSelected = ::onHrDeviceSelected,
            onRequestScanning = ::requestScanning,
            onDismissDialog = ::dismissDialog,
            onCancelScanning = ::cancelScanning,
            openSettings = ::openSettings,
            onActivityNameChanged = ::onActivityNameChanged,
            onActivityNameConfirmed = ::stopRecording,
        )
    }
}

@Composable
private fun DeviceSection(device: BleDevice?, onConnectClick: () -> Unit) {
    if (device == null) {
        HrButton(
            modifier = Modifier.height(Dimen48),
            onClick = onConnectClick,
            enabled = true,
        ) {
            Text(
                modifier = Modifier.padding(horizontal = Dimen32),
                text = "Connect Device".uppercase(),
                style = LabelSmall.copy(color = Red.copy(alpha = it)),
            )
        }
    } else {
        Text(
            modifier = Modifier.padding(Dimen32),
            text = "${device.name} from ${device.manufacturer}",
            style = LabelMediumBold.copy(color = White.copy(alpha = 0.7f))
        )
    }
}

@Composable
private fun Dialog(
    state: RecordScreenState,
    onDeviceSelected: (BleDevice) -> Unit,
    onRequestScanning: () -> Unit,
    onDismissDialog: () -> Unit,
    onCancelScanning: () -> Unit,
    openSettings: () -> Unit,
    onActivityNameChanged: (String) -> Unit,
    onActivityNameConfirmed: (String) -> Unit
) {
    when (val dialog = state.dialog) {
        is RecordScreenDialog.ChooseHRDevice -> {
            HrConnectDialog(
                isLoading = dialog.isLoading,
                devices = dialog.scannedDevices,
                isDeviceConfirmed = dialog.isDeviceConfirmed,
                loadingDuration = dialog.loadingDuration,
                onConfirmClick = onDeviceSelected,
                onRefresh = onRequestScanning,
                onDismissRequest = {
                    onDismissDialog()
                    onCancelScanning()
                },
            )
        }

        is RecordScreenDialog.DeviceConnectedDialog -> {
            InfoDialog(
                title = Res.string.dialog_device_connected_title,
                message = stringResource(
                    Res.string.dialog_device_connected_message,
                    dialog.bleDevice.name,
                    dialog.bleDevice.manufacturer.orEmpty()
                ),
                onDismiss = onDismissDialog
            )
            val haptic = LocalHapticFeedback.current
            LaunchedEffect(dialog) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                delay(DOUBLE_HAPTIC_INTERVAL)
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }

        is RecordScreenDialog.OpenSettingsDialog -> {
            InfoDialog(
                title = Res.string.dialog_open_setting_title,
                message = stringResource(Res.string.dialog_open_setting_message),
                buttonText = Res.string.dialog_open_setting_button_text,
                onDismiss = onDismissDialog,
                onButtonClick = {
                    openSettings()
                    onDismissDialog()
                }
            )
        }

        is RecordScreenDialog.NameActivity -> {
            NameActivityDialog(
                title = Res.string.dialog_name_activity_title,
                message = Res.string.dialog_name_activity_message,
                name = dialog.activityName,
                error = dialog.error,
                onNameChanged = onActivityNameChanged,
                onDismiss = onDismissDialog,
                onButtonClick = onActivityNameConfirmed
            )
        }

        is RecordScreenDialog.ConnectionErrorDialog -> {
            InfoDialog(
                title = Res.string.dialog_device_connection_failed_title,
                message = stringResource(Res.string.dialog_device_connection_failed_message),
                buttonText = Res.string.dialog_info_ok,
                onDismiss = onDismissDialog,
                onButtonClick = onDismissDialog,
            )
        }

        else -> {}
    }
}
