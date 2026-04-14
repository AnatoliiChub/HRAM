package com.achub.hram.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.achub.hram.ext.permissionController
import com.achub.hram.ext.requestBluetooth
import com.achub.hram.ext.toDto
import com.achub.hram.screen.activities.ActivitiesScreen
import com.achub.hram.screen.record.Dialogs
import com.achub.hram.screen.record.RecordViewModel
import com.achub.hram.screen.record.heartIconCenter
import com.achub.hram.style.Black
import com.achub.hram.style.Dimen24
import com.achub.hram.style.Dimen8
import com.achub.hram.style.Dimen96
import com.achub.hram.view.section.DeviceSection
import com.achub.hram.view.section.RecordSection
import com.achub.hram.view.section.TrackingIndicationsSection
import com.achub.hram.view.shader.ProperLiquidWaveEffect
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun MainScreenDesktop() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .background(color = Black)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .fillMaxSize()
        ) {
            ActivitiesScreen()
            RecordScreen(modifier = Modifier.align(Alignment.BottomCenter))
        }
    }
}

@Composable
fun RecordScreen(modifier: Modifier = Modifier) {
    val controller = permissionController()
    val viewModel = koinViewModel<RecordViewModel>(parameters = { parametersOf(controller) })
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    with(viewModel) {
        if (state.requestBluetooth) {
            requestBluetooth(onRequested = { clearRequestBluetooth() })
        }
        val indications = state.bleNotification.toDto()
        var heartGlobalCenter by remember { mutableStateOf(Offset.Unspecified) }
        var boxGlobalPosition by remember { mutableStateOf(Offset.Zero) }

        val device = state.connectedDevice?.toDto()
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .onGloballyPositioned { boxGlobalPosition = it.positionOnScreen() }
        ) {
            val hrNotification = indications.hrNotification
            ProperLiquidWaveEffect(
                center = heartIconCenter(heartGlobalCenter, boxGlobalPosition),
                apply = hrNotification?.hrBpm != null && hrNotification.isContactOn,
                minRadius = Dimen24
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Transparent,
                                0.38f to Black,
                                1.0f to Black,
                            )
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Dimen96, bottom = Dimen8),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Spacer(Modifier.weight(2f))
                        Box(Modifier.weight(6f)) {
                            TrackingIndicationsSection(indications, heartPosUpdated = { heartGlobalCenter = it })
                        }
                        Spacer(Modifier.weight(0.5f))
                        Column(Modifier.weight(6f), horizontalAlignment = Alignment.CenterHorizontally) {
                            DeviceSection(
                                device,
                                onConnectClick = { requestScanning() },
                                onDisconnectClick = { disconnect() },
                            )
                        }
                        Spacer(Modifier.weight(0.5f))
                        Box(Modifier.weight(6f), contentAlignment = Alignment.Center) {
                            RecordSection(
                                modifier = Modifier.padding(0.dp),
                                recordingState = state.recordingState,
                                onPlay = ::toggleRecording,
                                onStop = ::showNameActivityDialog,
                                isRecordingEnabled = state.isRecordingEnabled
                            )
                        }
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
        Dialogs(
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
@Preview
fun MainScreenPreview() {
    MainScreenDesktop()
}
