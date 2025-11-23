package com.achub.hram.screen.record

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.achub.hram.data.models.BleDevice
import com.achub.hram.data.models.HrIndication
import com.achub.hram.data.models.Indications
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
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.Duration.Companion.seconds

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
    val indications = state.indications
    val trackingStatus = state.trackingStatus
    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(Dimen16),
            horizontalAlignment = CenterHorizontally
        ) {
            TrackingIndicationsSection(indications)
            Spacer(Modifier.weight(1f))
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

        is RecordScreenDialog.DeviceConnectedDialog -> InfoDialog(
            title = "Device connected",
            message = "${dialog.bleDevice.name} from ${dialog.bleDevice.manufacturer.orEmpty()} successfully connected",
            onDismiss = onDismissDialog
        )

        is RecordScreenDialog.OpenSettingsDialog -> InfoDialog(
            title = "Provide permission please",
            message = "It looks like you denied ble permission explicitly. Please provide ble permission for the app in the settings.",
            buttonText = "Open Settings",
            onDismiss = onDismissDialog,
            onButonClick = {
                openSettings()
                onDismissDialog()
            }
        )

        is RecordScreenDialog.NameActivity -> {
            NameActivityDialog(
                title = "Name your activity",
                message = "Please enter a name for your activity before saving.",
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
            indications = Indications(
                hrIndication = HrIndication(hrBpm = 83, batteryLevel = 75),
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
            indications = Indications(
                hrIndication = HrIndication.Empty,
                elapsedTime = 754L,
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
            indications = Indications(
                hrIndication = HrIndication(hrBpm = 83, batteryLevel = 75),
                elapsedTime = 754L,
            ),
            trackingStatus = TrackingStatus(
                trackHR = false,
                trackGps = false,
                hrDevice = null
            ),
            recordingState = RecordingState.Init,
            dialog = RecordScreenDialog.ChooseHRDevice(
                isLoading = true,
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
