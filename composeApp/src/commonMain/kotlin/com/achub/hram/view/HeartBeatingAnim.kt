package com.achub.hram.view

import androidx.compose.animation.core.KeyframesSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.achub.hram.view.indications.IndicationImage
import hram.composeapp.generated.resources.Res
import hram.composeapp.generated.resources.ic_heart

private const val MIN_HEART_SCALE = 0.75f
private const val MAX_HEART_SCALE = 1.25f
private const val DEFAULT_HEART_SCALE = 1f

@Composable
fun HeartBeatingAnim(
    hrBpm: Int,
    modifier: Modifier,
    color: Color
) {
    val infiniteTransition = rememberInfiniteTransition()
    val animatedScale by infiniteTransition.animateFloat(
        initialValue = MIN_HEART_SCALE,
        targetValue = MAX_HEART_SCALE,
        animationSpec = infiniteRepeatable(
            animation = heartBeatingSpec(),
            repeatMode = RepeatMode.Restart
        )
    )
    val isBeating = hrBpm > 0
    IndicationImage(
        modifier = modifier.scale(if (isBeating) animatedScale else DEFAULT_HEART_SCALE),
        imageSize = 96.dp,
        drawable = Res.drawable.ic_heart,
        color = color
    )
}

private fun heartBeatingSpec(): KeyframesSpec<Float> {
    val duration = 1000
    val second = (duration * 0.24f).toInt()
    val third = (duration * 0.42f).toInt()
    val fourth = (duration * 0.58f).toInt()
    val fifth = (duration * 0.68f).toInt()
    return keyframes {
        durationMillis = duration
        0.8f at 0 using LinearEasing
        1.2f at second using LinearEasing
        0.8f at third using LinearEasing
        1.2f at fourth using LinearEasing
        0.9f at fifth using LinearEasing
        0.8f at duration using LinearEasing
    }
}
