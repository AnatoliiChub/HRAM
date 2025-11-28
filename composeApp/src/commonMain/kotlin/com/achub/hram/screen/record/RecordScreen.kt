package com.achub.hram.screen.record

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.achub.hram.ble.model.BleDevice
import com.achub.hram.ble.model.BleIndication
import com.achub.hram.ble.model.HrIndication
import com.achub.hram.data.models.TrackingStatus
import com.achub.hram.permissionController
import com.achub.hram.requestBluetooth
import com.achub.hram.style.BlackPreview
import com.achub.hram.style.Dimen16
import com.achub.hram.view.dialogs.HrConnectDialog
import com.achub.hram.view.dialogs.InfoDialog
import com.achub.hram.view.dialogs.NameActivityDialog
import com.achub.hram.view.section.RecordSection
import com.achub.hram.view.section.RecordingState
import com.achub.hram.view.section.TrackingIndicationsSection
import com.achub.hram.view.section.TrackingStatusCheckBoxSection
import hram.composeapp.generated.resources.Res
import hram.composeapp.generated.resources.dialog_device_connected_message
import hram.composeapp.generated.resources.dialog_device_connected_title
import hram.composeapp.generated.resources.dialog_name_activity_message
import hram.composeapp.generated.resources.dialog_name_activity_title
import hram.composeapp.generated.resources.dialog_open_setting_button_text
import hram.composeapp.generated.resources.dialog_open_setting_message
import hram.composeapp.generated.resources.dialog_open_setting_title
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.Duration.Companion.seconds

const val DOUBLE_HAPTIC_INTERVAL = 150L

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
        RecordScreenContent(
            state,
            onHrCheckBox = ::toggleHRTracking,
            onPlay = ::toggleRecording,
            onStop = ::showNameActivityDialog,
            onDismissDialog = ::dismissDialog,
            onCancelScanning = ::cancelScanning,
            onDeviceSelected = ::onHrDeviceSelected,
            onRequestScanning = ::requestScanning,
            openSettings = ::openSettings,
            onActivityNameChanged = ::onActivityNameChanged,
            onActivityNameConfirmed = ::stopRecording,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordScreenContent(
    state: RecordScreenState,
    onHrCheckBox: () -> Unit,
    onPlay: () -> Unit,
    onStop: () -> Unit,
    onCancelScanning: () -> Unit,
    onDismissDialog: () -> Unit,
    onDeviceSelected: (BleDevice) -> Unit,
    onRequestScanning: () -> Unit,
    openSettings: () -> Unit,
    onActivityNameChanged: (String) -> Unit,
    onActivityNameConfirmed: (String) -> Unit,
) {
    val isCheckBoxEnabled = state.recordingState == RecordingState.Init
    val indications = state.bleIndication
    val trackingStatus = state.trackingStatus
    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(Dimen16),
            horizontalAlignment = CenterHorizontally
        ) {
            Spacer(Modifier.weight(0.5f))
            TrackingIndicationsSection(indications)
            Spacer(Modifier.weight(0.5f))
            TrackingStatusCheckBoxSection(trackingStatus, isCheckBoxEnabled, onHrCheckBox)
            Spacer(Modifier.height(Dimen16))
            RecordSection(
                recordingState = state.recordingState,
                isRecordingAvailable = trackingStatus.atLeastOneTrackingEnabled,
                onPlay = onPlay,
                onStop = onStop
            )
        }
    }
    when (val dialog = state.dialog) {
        is RecordScreenDialog.ChooseHRDevice -> HrConnectDialog(
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

        is RecordScreenDialog.OpenSettingsDialog -> InfoDialog(
            title = Res.string.dialog_open_setting_title,
            message = stringResource(Res.string.dialog_open_setting_message),
            buttonText = Res.string.dialog_open_setting_button_text,
            onDismiss = onDismissDialog,
            onButonClick = {
                openSettings()
                onDismissDialog()
            }
        )

        is RecordScreenDialog.NameActivity -> {
            NameActivityDialog(
                title = Res.string.dialog_name_activity_title,
                message = Res.string.dialog_name_activity_message,
                name = dialog.activityName,
                error = dialog.error,
                onNameChanged = onActivityNameChanged,
                onDismiss = onDismissDialog,
                onButonClick = onActivityNameConfirmed
            )
        }

        else -> {}
    }
}

@Composable
@Preview(backgroundColor = BlackPreview, showBackground = true)
private fun RecordScreenPreview() {
    RecordScreenContent(
        state = RecordScreenState(
            bleIndication = BleIndication(
                hrIndication = HrIndication(hrBpm = 83, isContactOn = true, isSensorContactSupported = true),
                batteryLevel = 75,
                isBleConnected = true,
                elapsedTime = 235L,
            ),
            trackingStatus = TrackingStatus(
                trackHR = true,
                trackGps = true,
                hrDevice = BleDevice("Hrm 1", "00:11:22:33:44:55")
            ),
            recordingState = RecordingState.Recording
        ),
        onHrCheckBox = {},
        onPlay = {},
        onStop = {},
        onDismissDialog = {},
        onDeviceSelected = {},
        onRequestScanning = {},
        onCancelScanning = {},
        openSettings = {},
        onActivityNameChanged = {},
        onActivityNameConfirmed = {}
    )
}

@Composable
@Preview(backgroundColor = 0xFF000000, showBackground = true)
private fun RecordScreenEmptyPreview() {
    RecordScreenContent(
        state = RecordScreenState(
            bleIndication = BleIndication(
                hrIndication = null,
                batteryLevel = 75,
                isBleConnected = false,
                elapsedTime = 235L,
            ),
            trackingStatus = TrackingStatus(
                trackHR = true,
                trackGps = true,
                hrDevice = BleDevice("Hrm 1", "00:11:22:33:44:55")
            ),
            recordingState = RecordingState.Recording
        ),
        onHrCheckBox = {},
        onPlay = {},
        onStop = {},
        onDismissDialog = {},
        onDeviceSelected = {},
        onRequestScanning = {},
        onCancelScanning = {},
        openSettings = {},
        onActivityNameChanged = {},
        onActivityNameConfirmed = {}
    )
}

@Composable
@Preview
private fun RecordScreenChooseDeviceDialogPreview() {
    RecordScreenContent(
        state = RecordScreenState(
            bleIndication = BleIndication(
                hrIndication = HrIndication(hrBpm = 83, isContactOn = true, isSensorContactSupported = true),
                batteryLevel = 75,
                isBleConnected = true,
                elapsedTime = 235L,
            ),
            trackingStatus = TrackingStatus(
                trackHR = false,
                trackGps = false,
                hrDevice = null
            ),
            recordingState = RecordingState.Init,
            dialog = RecordScreenDialog.ChooseHRDevice(
                isLoading = false,
                scannedDevices = listOf(
                    BleDevice("Hrm1", "00:11:22:33:44:55"),
                    BleDevice("Hrm2", "66:77:88:99:AA:BB"),
                    BleDevice("Hrm3", "CC:DD:EE:FF:00:11"),
                    BleDevice("Hrm4", "22:33:44:55:66:77"),
                    BleDevice("Hrm5", "88:99:AA:BB:CC:DD"),

                ),
                loadingDuration = 5.seconds
            )

        ),
        onHrCheckBox = {},
        onPlay = {},
        onStop = {},
        onDismissDialog = {},
        onDeviceSelected = {},
        onRequestScanning = {},
        onCancelScanning = {},
        openSettings = {},
        onActivityNameChanged = {},
        onActivityNameConfirmed = {}
    )
}
