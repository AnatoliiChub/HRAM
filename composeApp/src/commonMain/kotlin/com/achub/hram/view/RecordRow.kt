package com.achub.hram.view

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
import androidx.compose.ui.unit.dp
import com.achub.hram.style.Red
import hram.composeapp.generated.resources.Res
import hram.composeapp.generated.resources.ic_pause
import hram.composeapp.generated.resources.ic_play
import hram.composeapp.generated.resources.ic_stop
import org.jetbrains.compose.resources.DrawableResource
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

        HrButton(
            modifier = Modifier.height(48.dp).fillMaxWidth(first),
            onClick = onPlay,
            enabled = (recordingState == RecordingState.Init && isRecordingAvailable.not()).not()
        ) { Icon(icon, it) }

        Spacer(Modifier.size(16.dp))

        HrButton(
            modifier = Modifier.height(48.dp).fillMaxWidth(second),
            onClick = onStop,
        ) { Icon(Res.drawable.ic_stop, it) }
    }
}

@Composable
private fun Icon(icon: DrawableResource, f: Float) {
    Icon(
        modifier = Modifier.padding(2.dp),
        painter = painterResource(icon),
        tint = Red.copy(alpha = f),
        contentDescription = null
    )
}

enum class RecordingState {
    Recording, Paused, Init;

    fun isRecording() = this == Recording
}