package com.achub.hram.screen.record

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.achub.hram.data.model.BleDevice
import com.achub.hram.data.model.TrackingIndications
import com.achub.hram.data.model.TrackingStatus
import com.achub.hram.permissionController
import com.achub.hram.view.RecordRow
import com.achub.hram.view.RecordingState
import com.achub.hram.view.dialog.ChooseHRDeviceDialog
import com.achub.hram.view.section.TrackingIndicationsSection
import com.achub.hram.view.section.TrackingStatusCheckBoxSection
import hram.composeapp.generated.resources.Res
import hram.composeapp.generated.resources.ic_record
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.core.parameter.parametersOf

object RecordScreen : Tab {
    @Composable
    override fun Content() {
        val controller = permissionController()
        val viewModel = koinScreenModel<RecordViewModel>(parameters = { parametersOf(controller) })
        val state = viewModel.uiState.collectAsStateWithLifecycle().value
        with(viewModel) {
            RecordScreenContent(
                state,
                onHrCheckBox = ::toggleHRTracking,
                onLocationCheckBox = ::toggleLocationTracking,
                onPlay = ::onPlay,
                onStop = ::onStop,
                onDismissDialog = {
                    dismissDialog()
                    cancelScanning()
                },
                onDeviceSelected = ::onDeviceSelected,
                onRequestScanning = ::requestScanning
            )
        }
    }

    override val options: TabOptions
        @Composable
        get() {
            val title = "Recording"
            val icon = rememberVectorPainter(vectorResource(Res.drawable.ic_record))

            return remember {
                TabOptions(
                    index = 1u,
                    title = title,
                    icon = icon
                )
            }
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordScreenContent(
    state: RecordScreenState,
    onHrCheckBox: () -> Unit,
    onLocationCheckBox: () -> Unit,
    onPlay: () -> Unit,
    onStop: () -> Unit,
    onDismissDialog: () -> Unit,
    onDeviceSelected: (BleDevice) -> Unit,
    onRequestScanning: () -> Unit
) {
    val isCheckBoxEnabled = state.recordingState == RecordingState.Init
    val indications = state.indications
    val trackingStatus = state.trackingStatus
    Column(
        modifier = Modifier.fillMaxSize().padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
        horizontalAlignment = CenterHorizontally
    ) {
        TrackingIndicationsSection(indications)
        Spacer(Modifier.weight(1f))
        TrackingStatusCheckBoxSection(trackingStatus, isCheckBoxEnabled, onHrCheckBox, onLocationCheckBox)
        RecordRow(
            recordingState = state.recordingState,
            isRecordingAvailable = trackingStatus.atLeastOneTrackingEnabled,
            onPlay = onPlay,
            onStop = onStop
        )
    }
    Dialogs(state.dialog, onDeviceSelected, onDismissDialog, onRequestScanning)
}

@Composable
private fun Dialogs(
    dialog: RecordScreenDialog?,
    onDeviceSelected: (BleDevice) -> Unit,
    onDismissDialog: () -> Unit,
    onRequestScanning: () -> Unit
) {
    if (dialog is RecordScreenDialog.ChooseHRDevice) {
        dialog
        ChooseHRDeviceDialog(
            onConfirmClick = onDeviceSelected,
            onDismissRequest = onDismissDialog,
            isLoading = dialog.isLoading,
            onRefresh = onRequestScanning,
            devices = dialog.scannedDevices,
        )
    }
}

@Composable
@Preview
private fun RecordScreenPreview() {
    RecordScreenContent(
        state = RecordScreenState(
            indications = TrackingIndications(
                heartRate = 83,
                distance = 1.2f,
                duration = "00:12:34",
            ),
            trackingStatus = TrackingStatus(
                trackHR = true,
                trackGps = true,
                hrDevice = BleDevice("Hrm 1", "00:11:22:33:44:55")
            ),
            recordingState = RecordingState.Recording
        ),
        onHrCheckBox = {},
        onLocationCheckBox = {},
        onPlay = {},
        onStop = {},
        onDismissDialog = {},
        onDeviceSelected = {},
        onRequestScanning = {}
    )
}

@Composable
@Preview
private fun RecordScreenChooseDeviceDialogPreview() {
    RecordScreenContent(
        state = RecordScreenState(
            indications = TrackingIndications(
                heartRate = 83,
                distance = 1.2f,
                duration = "00:12:34",
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
            )

        ),
        onHrCheckBox = {},
        onLocationCheckBox = {},
        onPlay = {},
        onStop = {},
        onDismissDialog = {},
        onDeviceSelected = {},
        onRequestScanning = {}
    )
}
