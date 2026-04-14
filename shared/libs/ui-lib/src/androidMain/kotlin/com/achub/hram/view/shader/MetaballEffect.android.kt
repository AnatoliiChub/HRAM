package com.achub.hram.view.shader

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.unit.IntSize

@Composable
actual fun rememberMetaballRenderEffect(
    shaderSrc: String,
    center1: Offset,
    center2: Offset,
    radius: Float,
    topColor: Color,
    bottomColor: Color,
    borderColor: Color,
    resolution: IntSize,
): androidx.compose.ui.graphics.RenderEffect? {
    // RuntimeShader is expensive to create — recreate only when source changes.
    val runtimeShader = remember(shaderSrc) { RuntimeShader(shaderSrc) }

    // Update uniforms and wrap in a new RenderEffect whenever any input changes.
    return remember(runtimeShader, center1, center2, radius, topColor, bottomColor, borderColor, resolution) {
        runtimeShader.setFloatUniform(
            "resolution",
            resolution.width.toFloat(),
            resolution.height.toFloat(),
        )
        runtimeShader.setFloatUniform("center1", center1.x, center1.y)
        runtimeShader.setFloatUniform("center2", center2.x, center2.y)
        runtimeShader.setFloatUniform("radius", radius)
        // float4 uniforms require the array overload
        runtimeShader.setFloatUniform(
            "topColor",
            floatArrayOf(topColor.red, topColor.green, topColor.blue, topColor.alpha),
        )
        runtimeShader.setFloatUniform(
            "bottomColor",
            floatArrayOf(bottomColor.red, bottomColor.green, bottomColor.blue, bottomColor.alpha),
        )
        runtimeShader.setFloatUniform(
            "borderColor",
            floatArrayOf(borderColor.red, borderColor.green, borderColor.blue, borderColor.alpha),
        )

        RenderEffect.createRuntimeShaderEffect(runtimeShader, "content")
            .asComposeRenderEffect()
    }
}
