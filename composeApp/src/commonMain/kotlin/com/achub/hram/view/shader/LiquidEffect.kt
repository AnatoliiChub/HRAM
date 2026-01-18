package com.achub.hram.view.shader

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.unit.IntSize

@Composable
expect fun rememberLiquidRenderEffect(
    shaderSrc: String,
    time: Float,
    density: Float,
    center: Offset = Offset.Unspecified,
    baseColor: Color,
    resolution: IntSize = IntSize.Zero,
    minRadius: Float,
): RenderEffect?
