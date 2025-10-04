package com.achub.hram.view

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.unit.dp
import com.achub.hram.style.Red
import kotlin.time.Duration

private const val MIN_PROGRESS = 0.1f
private const val MAX_PROGRESS = 1.1f

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HRProgress(isLoading: Boolean, cycleDuration: Duration) {
    val progress = remember { Animatable(MAX_PROGRESS) }

    val reset = derivedStateOf { progress.value == MAX_PROGRESS }
    LaunchedEffect(reset.value) {
        if (reset.value) progress.snapTo(MIN_PROGRESS)
        if (isLoading) {
            progress.animateTo(
                targetValue = MAX_PROGRESS,
                animationSpec = tween(durationMillis = cycleDuration.inWholeMilliseconds.toInt(), easing = LinearEasing)
            )
        }
    }

    LinearWavyProgressIndicator(
        modifier = Modifier.height(48.dp).fillMaxWidth(),
        trackColor = Transparent,
        color = Red,
        gapSize = 8.dp,
        stopSize = 0.dp,
        wavelength = 34.dp,
        waveSpeed = 2.dp,
        progress = { progress.value }
    )
}