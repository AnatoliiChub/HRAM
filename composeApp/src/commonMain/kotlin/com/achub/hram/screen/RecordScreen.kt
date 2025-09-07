package com.achub.hram.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.unit.dp
import com.achub.hram.style.Heading1
import com.achub.hram.style.White
import com.achub.hram.view.DistanceLabelRow
import com.achub.hram.view.HeartLabelRow
import com.achub.hram.view.RecordRow
import com.achub.hram.view.RecordingState
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun RecordScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(top = 32.dp),
        horizontalAlignment = CenterHorizontally
    ) {
        var recordingState by remember { mutableStateOf(RecordingState.Init) }

        Column {
            HeartLabelRow("88")
            DistanceLabelRow("1.43 km")
            Text(
                modifier = Modifier.align(CenterHorizontally),
                text = "15:53",
                style = Heading1.copy(color = White, fontWeight = W500)
            )
        }
        Spacer(Modifier.weight(1f))
        RecordRow(recordingState, onPlay = {
            recordingState = if (recordingState == RecordingState.Recording) {
                RecordingState.Paused
            } else {
                RecordingState.Recording
            }
        }, onStop = { recordingState = RecordingState.Init })
    }
}

@Composable
@Preview
fun RecordScreenPreview() {
    RecordScreen()
}
