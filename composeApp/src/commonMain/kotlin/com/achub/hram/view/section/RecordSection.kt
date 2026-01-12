package com.achub.hram.view.section

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.achub.hram.style.Dimen16
import com.achub.hram.style.Dimen2
import com.achub.hram.style.Dimen48
import com.achub.hram.style.Red
import com.achub.hram.view.components.HrButton
import hram.composeapp.generated.resources.Res
import hram.composeapp.generated.resources.ic_pause
import hram.composeapp.generated.resources.ic_play
import hram.composeapp.generated.resources.ic_stop
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

const val PAUSED_STATE_BTN_WIDTH_FRACTION = 0.5f

@Composable
fun RecordSection(
    recordingState: RecordingState,
    onPlay: () -> Unit = {},
    onStop: () -> Unit = {},
    isRecordingEnabled: Boolean,
) {
    Row(modifier = Modifier.padding(Dimen16)) {
        val icon = if (recordingState == RecordingState.Recording) Res.drawable.ic_pause else Res.drawable.ic_play
        val pausedWidthFraction = if (recordingState == RecordingState.Paused) PAUSED_STATE_BTN_WIDTH_FRACTION else 1f
        val first by animateFloatAsState(pausedWidthFraction)
        val second by animateFloatAsState(if (recordingState == RecordingState.Paused) 1f else 0f)

        HrButton(
            modifier = Modifier.height(Dimen48).fillMaxWidth(first),
            onClick = onPlay,
            enabled = isRecordingEnabled,
        ) { Icon(icon, it) }

        Spacer(Modifier.size(Dimen16))

        HrButton(
            modifier = Modifier.height(Dimen48).fillMaxWidth(second),
            onClick = onStop,
        ) { Icon(Res.drawable.ic_stop, it) }
    }
}

@Composable
private fun Icon(icon: DrawableResource, f: Float) {
    Icon(
        modifier = Modifier.padding(Dimen2),
        painter = painterResource(icon),
        tint = Red.copy(alpha = f),
        contentDescription = null
    )
}

enum class RecordingState {
    Recording,
    Paused,
    Init;

    fun isRecording() = this == Recording
}

@Preview
@Composable
private fun RecordSectionRecordingPreview() {
    RecordSection(
        recordingState = RecordingState.Recording,
        onPlay = {},
        onStop = {},
        isRecordingEnabled = true
    )
}

@Preview
@Composable
private fun RecordSectionPausedPreview() {
    RecordSection(
        recordingState = RecordingState.Paused,
        onPlay = {},
        onStop = {},
        isRecordingEnabled = true
    )
}

@Preview
@Composable
private fun RecordSectionInitPreview() {
    RecordSection(
        recordingState = RecordingState.Init,
        onPlay = {},
        onStop = {},
        isRecordingEnabled = false
    )
}
