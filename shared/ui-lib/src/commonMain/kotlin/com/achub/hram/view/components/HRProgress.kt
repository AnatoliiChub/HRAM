package com.achub.hram.view.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.achub.hram.style.Dimen2
import com.achub.hram.style.Dimen24
import com.achub.hram.style.Dimen48
import com.achub.hram.style.Dimen8
import com.achub.hram.style.DimenZero
import com.achub.hram.style.Red
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private const val MIN_PROGRESS = 0.1f
private const val MAX_PROGRESS = 1.1f
private const val VISIBILITY_MARGIN = 0.05f

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HRProgress(isLoading: Boolean, cycleDuration: Duration, height: Dp = Dimen48) {
    val progress = remember { Animatable(MAX_PROGRESS) }

    val reset = derivedStateOf { progress.value >= MAX_PROGRESS }
    LaunchedEffect(reset.value, isLoading) {
        if (reset.value || isLoading) progress.snapTo(MIN_PROGRESS)
        if (isLoading) {
            progress.animateTo(
                targetValue = MAX_PROGRESS,
                animationSpec = tween(durationMillis = cycleDuration.inWholeMilliseconds.toInt(), easing = LinearEasing)
            )
        }
    }

    val isVisible = progress.value > MIN_PROGRESS && progress.value < MAX_PROGRESS - VISIBILITY_MARGIN

    val alpha = animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 300,
        )
    )
    LinearWavyProgressIndicator(
        modifier = Modifier.fillMaxWidth().height(height).alpha(alpha.value),
        trackColor = Transparent,
        color = Red,
        gapSize = Dimen8,
        stopSize = DimenZero,
        wavelength = Dimen24,
        waveSpeed = Dimen2,
        progress = { progress.value },
    )
}

@Preview
@Composable
private fun HRProgressPreview() {
    HRProgress(isLoading = true, cycleDuration = 5.seconds)
}
