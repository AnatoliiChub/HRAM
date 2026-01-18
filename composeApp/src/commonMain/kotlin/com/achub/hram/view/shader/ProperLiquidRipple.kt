package com.achub.hram.view.shader

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.achub.hram.style.DarkRed
import com.achub.hram.view.shader

/**
 *  if center is Offset.Unspecified, no ripple is drawn
 *  else a ripple is drawn originating from the specified center point
 */
fun Modifier.liquidRipple(
    center: Offset = Offset.Unspecified,
    baseColor: Color = DarkRed,
    minRadius: Dp = 0.dp
): Modifier = composed {
    // Track size so we can provide rects to the shader and center calculations if needed
    var size by remember { mutableStateOf(IntSize.Zero) }

    val density = LocalDensity.current.density

    // One-shot normalized animated time [0f..1f] triggered when `center` changes to a valid value
    val progress = remember { Animatable(0f) }
    // internal duration for the ripple (ms) — kept inside the implementation
    val internalDuration = 1500
    LaunchedEffect(center) {
        if (center != Offset.Unspecified) {
            progress.snapTo(0f)
            progress.animateTo(1f, animationSpec = tween(durationMillis = internalDuration, easing = LinearEasing))
            // after animation finishes, reset to 0 so it can replay on next tap
            progress.snapTo(0f)
        } else {
            progress.snapTo(0f)
        }
    }
    val animatedProgress = progress.value

    val minRadiusPx = with(LocalDensity.current) { minRadius.toPx() }

    val effect = shader("files/shaders/liquidRipple.agsl")?.let { shader ->
        rememberLiquidRenderEffect(
            shaderSrc = shader,
            time = animatedProgress,
            density = density,
            center = if (center != Offset.Unspecified) center else Offset(size.width / 2f, size.height / 2f),
            baseColor = baseColor,
            resolution = size,
            minRadius = minRadiusPx
        )
    }

    this.onGloballyPositioned { coords ->
        size = coords.size
    }.graphicsLayer {
        val active = animatedProgress > 0f && center != Offset.Unspecified
        this.renderEffect = if (active) effect else null
    }
}
