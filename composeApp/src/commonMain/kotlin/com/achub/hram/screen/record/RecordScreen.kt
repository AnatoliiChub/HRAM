package com.achub.hram.screen.record

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import hram.composeapp.generated.resources.Res
import hram.composeapp.generated.resources.ic_record
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview

object RecordScreen : Tab {
    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<RecordViewModel>()
        val state = viewModel.uiState.collectAsStateWithLifecycle().value
        RecordScreenContent(state, viewModel.actionHandler!!)
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

@Composable
private fun RecordScreenContent(
    state: RecordScreenState,
    action: RecordActionHandler
) {
    val isCheckBoxEnabled = state.recordingState == RecordingState.Init
    Column(
        modifier = Modifier.fillMaxSize().padding(top = 32.dp),
        horizontalAlignment = CenterHorizontally
    ) {
        Column {
            HeartLabelRow(state.heartRate)
            DistanceLabelRow(state.distance)
            Text(
                modifier = Modifier.align(CenterHorizontally),
                text = state.duration,
                style = Heading1.copy(color = White, fontWeight = W500)
            )
        }
        Spacer(Modifier.weight(1f))
        Column {
            HRCheckBoxLabel(state.trackHR, isCheckBoxEnabled) { action.toggleHRTracking() }
            LocationCheckBoxLabel(state.trackLocation, isCheckBoxEnabled) {
                action.toggleLocationTracking()
            }
            Spacer(Modifier.height(32.dp))
        }
        RecordRow(
            recordingState = state.recordingState,
            isRecordingAvailable = state.trackHR || state.trackLocation,
            onPlay = action::onPlay,
            onStop = action::onStop
        )
    }
}


@Composable
@Preview
fun RecordScreenPreview() {
    RecordScreenContent(
        RecordScreenState(
            recordingState = RecordingState.Recording,
            heartRate = 123,
            distance = 0.65f,
            duration = "00:02:30",
            trackHR = true,
            trackLocation = false
        ), action = object : RecordActionHandler {
            override fun onPlay() {}
            override fun onStop() {}
            override fun toggleHRTracking() {}
            override fun toggleLocationTracking() {}
        })
}
