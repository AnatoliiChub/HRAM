package com.achub.hram.view

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.achub.hram.style.Red
import com.achub.hram.style.White
import hram.composeapp.generated.resources.Res
import hram.composeapp.generated.resources.ic_pause
import hram.composeapp.generated.resources.ic_play
import hram.composeapp.generated.resources.ic_stop
import org.jetbrains.compose.resources.painterResource

@Composable
fun RecordRow(
    recordingState: RecordingState,
    isRecordingAvailable: Boolean,
    onPlay: () -> Unit = {},
    onStop: () -> Unit = {}
) {
    Row(modifier = Modifier.padding(16.dp)) {
        val icon = if (recordingState == RecordingState.Recording) Res.drawable.ic_pause
        else Res.drawable.ic_play
        val first by animateFloatAsState(if (recordingState == RecordingState.Paused) 0.5f else 1f)
        val second by animateFloatAsState(if (recordingState == RecordingState.Paused) 1f else 0f)
        FilledIconButton(
            modifier = Modifier.height(48.dp).fillMaxWidth(first).padding(end = 8.dp),
            onClick = onPlay,
            shape = RoundedCornerShape(8.dp),
            colors = IconButtonColors(Red, White, Red.copy(alpha = 0.2f), White),
            enabled = (recordingState == RecordingState.Init && isRecordingAvailable.not()).not()
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null
            )
        }
        FilledIconButton(
            modifier = Modifier.height(48.dp).fillMaxWidth(second).padding(start = 8.dp),
            onClick = onStop,
            shape = RoundedCornerShape(8.dp),
            colors = IconButtonColors(Red, White, Red, White)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_stop),
                contentDescription = null
            )
        }
    }
}

enum class RecordingState {
    Recording, Paused, Init;

    fun isRecording() = this == Recording
}