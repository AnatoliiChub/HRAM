package com.achub.hram.view.section

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.achub.hram.style.Dimen16
import com.achub.hram.style.Dimen2
import com.achub.hram.style.Dimen216
import com.achub.hram.style.Dimen64
import com.achub.hram.view.shader.MetaballContainer
import hram.ui_lib.generated.resources.Res
import hram.ui_lib.generated.resources.ic_pause
import hram.ui_lib.generated.resources.ic_play
import hram.ui_lib.generated.resources.ic_stop
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/** Tween used for the split / merge position animation. */
private val metaballTween = tween<Dp>(durationMillis = 650, easing = EaseInOut)

@Composable
fun RecordSection(
    modifier: Modifier = Modifier.padding(Dimen16),
    recordingState: RecordingState,
    onPlay: () -> Unit = {},
    onStop: () -> Unit = {},
    isRecordingEnabled: Boolean,
) {
    val isPaused = recordingState == RecordingState.Paused
    val playIcon =
        if (recordingState == RecordingState.Recording) Res.drawable.ic_pause else Res.drawable.ic_play

    val density = LocalDensity.current

    val totalWidth: Dp = Dimen216
    val gap: Dp = Dimen64
    val btnWidth: Dp = (totalWidth - gap) / 2

    val colorScheme = MaterialTheme.colorScheme

    // Plain Box with explicit dimensions — BoxWithConstraints (SubcomposeLayout) cannot
    // answer intrinsic-height queries and crashes on Desktop when a parent needs them.
    Box(modifier = modifier.width(totalWidth).height(btnWidth)) {
        // When merged: both buttons sit at the horizontal center so their blobs overlap.
        // When paused: play goes to the left edge, stop goes to the right.
        val mergedX: Dp = (totalWidth - btnWidth) / 2
        val playX by animateDpAsState(
            targetValue = if (isPaused) 0.dp else mergedX,
            animationSpec = metaballTween,
            label = "playX",
        )
        val stopX by animateDpAsState(
            targetValue = if (isPaused) btnWidth + gap else mergedX,
            animationSpec = metaballTween,
            label = "stopX",
        )

        val stopIconAlpha = ((stopX - playX - Dimen64) / (btnWidth + gap - Dimen64))
            .coerceIn(0f, 1f)

        val btnRadiusPx = with(density) { (btnWidth / 2).toPx() }
        val center1 = Offset(
            x = with(density) { (stopX + btnWidth / 2).toPx() },
            y = btnRadiusPx,
        )
        val center2 = Offset(
            x = with(density) { (playX + btnWidth / 2).toPx() },
            y = btnRadiusPx,
        )

        val light = if (isRecordingEnabled) colorScheme.primary else colorScheme.secondary
        val dark = if (isRecordingEnabled) colorScheme.secondary else colorScheme.secondary

        MetaballContainer(
            modifier = Modifier.width(totalWidth).height(btnWidth),
            center1 = center1,
            center2 = center2,
            radius = btnRadiusPx,
            borderColor = if (isRecordingEnabled) colorScheme.onBackground else colorScheme.onSurfaceVariant,
            topColor = dark,
            bottomColor = light,
        ) {
            // Stop button hit area
            Box(
                modifier = Modifier
                    .absoluteOffset(x = stopX)
                    .size(btnWidth)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onStop,
                    ),
            )
            // Play / pause button hit area.
            Box(
                modifier = Modifier
                    .absoluteOffset(x = playX)
                    .size(btnWidth)
                    .clickable(
                        enabled = isRecordingEnabled,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onPlay,
                    ),
            )
        }

        Box(
            modifier = Modifier.absoluteOffset(x = stopX).size(btnWidth),
            contentAlignment = Alignment.Center,
        ) {
            ButtonIcon(Res.drawable.ic_stop, colorScheme.onPrimary, stopIconAlpha)
        }
        Box(
            modifier = Modifier.absoluteOffset(x = playX).size(btnWidth),
            contentAlignment = Alignment.Center,
        ) {
            ButtonIcon(playIcon, if (isRecordingEnabled) colorScheme.onPrimary else colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ButtonIcon(icon: DrawableResource, color: Color, alpha: Float = 1f) {
    Icon(
        modifier = Modifier.padding(Dimen2),
        painter = painterResource(icon),
        tint = color.copy(alpha = alpha),
        contentDescription = null,
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
        isRecordingEnabled = true,
    )
}
