package com.achub.hram.view.shader

import android.content.res.Resources
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.IntSize

@Composable
actual fun rememberLiquidRenderEffect(
    shaderSrc: String,
    time: Float,
    density: Float,
    center: Offset,
    baseColor: Color,
    resolution: IntSize,
    minRadius: Float
): androidx.compose.ui.graphics.RenderEffect? {
    val runtimeShader = remember(shaderSrc) { RuntimeShader(shaderSrc) }

    // Resolve resolution (use provided resolution if available, otherwise fallback to screen)
    val resX = if (resolution.width > 0) {
        resolution.width.toFloat()
    } else {
        Resources.getSystem().displayMetrics.widthPixels.toFloat()
    }

    val resY = if (resolution.height > 0) {
        resolution.height.toFloat()
    } else {
        Resources.getSystem().displayMetrics.heightPixels.toFloat()
    }

    try {
        runtimeShader.setFloatUniform("time", time)
        runtimeShader.setFloatUniform("density", density)
        runtimeShader.setFloatUniform("minRadius", minRadius)
        runtimeShader.setFloatUniform("resolution", floatArrayOf(resX, resY))

        val cx = if (center != Offset.Unspecified) center.x else resX / 2f
        val cy = if (center != Offset.Unspecified) center.y else resY / 2f
        // normalized origin expected by shader
        val ox = cx / maxOf(1f, resX)
        val oy = cy / maxOf(1f, resY)
        runtimeShader.setFloatUniform("origin", floatArrayOf(ox, oy))

        runtimeShader.setColorUniform("color", baseColor.toArgb())
    } catch (_: Throwable) {
        // Ignore uniform failures on preview or older environments
    }

    return remember(runtimeShader, time, density, center, baseColor, resolution, minRadius) {
        RenderEffect.createRuntimeShaderEffect(runtimeShader, "content").asComposeRenderEffect()
    }
}
