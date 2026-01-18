package com.achub.hram.view.shader

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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

@Composable
fun ProperLiquidWaveEffect(
    baseColor: Color = DarkRed,
    durationMillis: Int = 4000,
    apply: Boolean,
    center: Offset = Offset.Unspecified,
    minRadius: Dp = 0.dp,
    content: @Composable BoxScope.() -> Unit
) {
    // Track size so we can provide rects to the shader and center calculations if needed
    var size by remember { mutableStateOf(IntSize.Zero) }

    val density = LocalDensity.current.density

    // Normalized animated time [0f..1f)
    val infinite = rememberInfiniteTransition()
    val animatedProgress by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = durationMillis, easing = LinearEasing))
    )

    val minRadiusPx = with(LocalDensity.current) { minRadius.toPx() }

    // Apply the renderEffect to the content layer so the shader modifies the content (liquid/distortion)
    Box(
        modifier = Modifier
            .onGloballyPositioned { coords ->
                size = coords.size
            }
            .fillMaxSize()
    ) {
        val effect = shader("files/shaders/liquidWaves.agsl")?.let { shader ->
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // Only set renderEffect when visible; graphicsLayer accepts null
                    this.renderEffect = if (apply) effect else null
                    this.alpha = alpha
                }
        ) {
            content()
        }
    }
}
