package com.achub.hram.view.components

import OnBackground
import androidx.compose.animation.core.KeyframesSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.achub.hram.style.Dimen76
import hram.composeapp.generated.resources.Res
import hram.composeapp.generated.resources.ic_heart
import org.jetbrains.compose.resources.DrawableResource

private const val MIN_HEART_SCALE = 0.75f
private const val MAX_HEART_SCALE = 1.25f
private const val DEFAULT_HEART_SCALE = 1f
private const val HEART_BEATING_CYCLE_MS = 1000

// fractional positions inside the cycle
private const val HEART_FRAME_2_FRACTION = 0.24f
private const val HEART_FRAME_3_FRACTION = 0.42f
private const val HEART_FRAME_4_FRACTION = 0.58f
private const val HEART_FRAME_5_FRACTION = 0.68f

// scale values used in keyframes
private const val HEART_SCALE_LOW = 0.8f
private const val HEART_SCALE_HIGH = 1.2f
private const val HEART_SCALE_MID = 0.9f

@Composable
fun HeartBeatingAnimView(
    isBeating: Boolean,
    modifier: Modifier,
    icon: DrawableResource,
    color: Color
) {
    var isAppInBackground by remember { mutableStateOf(true) }
    OnBackground { isAppInBackground = it }

    val animatedScale by if (isAppInBackground.not() && isBeating) {
        val infiniteTransition = rememberInfiniteTransition()
        infiniteTransition.animateFloat(
            initialValue = MIN_HEART_SCALE,
            targetValue = MAX_HEART_SCALE,
            animationSpec = infiniteRepeatable(
                animation = heartBeatingSpec(),
                repeatMode = RepeatMode.Restart
            )
        )
    } else {
        mutableStateOf(DEFAULT_HEART_SCALE)
    }

    IndicationImage(
        modifier = modifier.scale(if (isBeating) animatedScale else DEFAULT_HEART_SCALE),
        imageSize = Dimen76,
        drawable = icon,
        color = color
    )
}

private fun heartBeatingSpec(): KeyframesSpec<Float> {
    val duration = HEART_BEATING_CYCLE_MS
    val second = (duration * HEART_FRAME_2_FRACTION).toInt()
    val third = (duration * HEART_FRAME_3_FRACTION).toInt()
    val fourth = (duration * HEART_FRAME_4_FRACTION).toInt()
    val fifth = (duration * HEART_FRAME_5_FRACTION).toInt()
    return keyframes {
        durationMillis = duration
        HEART_SCALE_LOW at 0 using LinearEasing
        HEART_SCALE_HIGH at second using LinearEasing
        HEART_SCALE_LOW at third using LinearEasing
        HEART_SCALE_HIGH at fourth using LinearEasing
        HEART_SCALE_MID at fifth using LinearEasing
        HEART_SCALE_LOW at duration using LinearEasing
    }
}

@Preview
@Composable
fun HeartBeatingAnimViewPreview() {
    HeartBeatingAnimView(
        isBeating = true,
        modifier = Modifier,
        icon = Res.drawable.ic_heart,
        color = Color.Red
    )
}
