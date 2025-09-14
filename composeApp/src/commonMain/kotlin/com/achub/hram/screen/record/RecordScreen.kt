package com.achub.hram.screen.record

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.achub.hram.style.Heading1
import com.achub.hram.style.White
import com.achub.hram.view.DistanceLabelRow
import com.achub.hram.view.HRCheckBoxLabel
import com.achub.hram.view.HeartLabelRow
import com.achub.hram.view.LocationCheckBoxLabel
import com.achub.hram.view.RecordRow
import com.achub.hram.view.RecordingState
import com.achub.hram.view.WarningLabelRow
import com.achub.hram.view.dialog.ChooseHRDeviceDialog
import hram.composeapp.generated.resources.Res
import hram.composeapp.generated.resources.ic_record
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview

object RecordScreen : Tab {
    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<RecordViewModel>()
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
                onRequestScanning = ::onRequestScanning
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
    onDeviceSelected: (String) -> Unit,
    onRequestScanning: () -> Unit
) {

    val isCheckBoxEnabled = state.recordingState == RecordingState.Init
    val indications = state.indications
    val checkboxes = state.checkboxes
    val atLeastOneTrackingEnabled = checkboxes.trackHR || checkboxes.trackGps
    Column(
        modifier = Modifier.fillMaxSize().padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
        horizontalAlignment = CenterHorizontally
    ) {
        Column {
            HeartLabelRow(hrBpm = indications.heartRate)
            DistanceLabelRow(distance = indications.distance)
            Text(
                modifier = Modifier.align(CenterHorizontally),
                text = indications.duration,
                style = Heading1.copy(color = White, fontWeight = W500)
            )
        }
        Spacer(Modifier.weight(1f))
        if (atLeastOneTrackingEnabled.not()) {
            WarningLabelRow(label = "Choose at least one tracking option")
        }
        Column {
            HRCheckBoxLabel(
                isChecked = checkboxes.trackHR,
                isEnabled = isCheckBoxEnabled,
                connectedDevice = checkboxes.hrDevice
            ) { onHrCheckBox() }
            LocationCheckBoxLabel(
                isChecked = checkboxes.trackGps,
                isEnabled = isCheckBoxEnabled
            ) { onLocationCheckBox() }
            Spacer(Modifier.height(32.dp))
        }
        RecordRow(
            recordingState = state.recordingState,
            isRecordingAvailable = atLeastOneTrackingEnabled,
            onPlay = onPlay,
            onStop = onStop
        )
    }
    if (state.dialog is RecordScreenDialog.ChooseHRDevice) {
        state.dialog
        ChooseHRDeviceDialog(
            onConfirmClick = onDeviceSelected,
            onDismissRequest = onDismissDialog,
            isLoading = state.dialog.isLoading,
            onRefresh = onRequestScanning,
            devices = state.dialog.scannedDevices
        )
    }
}

@Composable
@Preview
private fun RecordScreenPreview() {
    RecordScreenContent(
        state = RecordScreenState(
            indications = RecordScreenIndications(
                heartRate = 83,
                distance = 1.2f,
                duration = "00:12:34",
            ),
            checkboxes = RecordScreenCheckboxes(
                trackHR = true,
                trackGps = true,
                hrDevice = "Polar H10"
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
            indications = RecordScreenIndications(
                heartRate = 83,
                distance = 1.2f,
                duration = "00:12:34",
            ),
            checkboxes = RecordScreenCheckboxes(
                trackHR = false,
                trackGps = false,
                hrDevice = null
            ),
            recordingState = RecordingState.Init,
            dialog = RecordScreenDialog.ChooseHRDevice(
                isLoading = true,
                scannedDevices = listOf("Device 1", "Device 2", "Device 3"),
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
