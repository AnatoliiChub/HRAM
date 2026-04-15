package com.achub.hram.view.shader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.unit.IntSize
import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder

// Desktop (JVM/Skiko) — identical to the iOS Skia implementation.
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
): RenderEffect? {
    val runtimeEffect = remember(shaderSrc) { RuntimeEffect.makeForShader(shaderSrc) }

    return remember(runtimeEffect, center1, center2, radius, topColor, bottomColor, borderColor, resolution) {
        val builder = RuntimeShaderBuilder(runtimeEffect)

        builder.uniform("resolution", resolution.width.toFloat(), resolution.height.toFloat())
        builder.uniform("center1", center1.x, center1.y)
        builder.uniform("center2", center2.x, center2.y)
        builder.uniform("radius", radius)
        builder.uniform("topColor", topColor.red, topColor.green, topColor.blue, topColor.alpha)
        builder.uniform(
            "bottomColor",
            bottomColor.red,
            bottomColor.green,
            bottomColor.blue,
            bottomColor.alpha,
        )
        builder.uniform(
            "borderColor",
            borderColor.red,
            borderColor.green,
            borderColor.blue,
            borderColor.alpha,
        )

        ImageFilter.makeRuntimeShader(
            runtimeShaderBuilder = builder,
            shaderNames = arrayOf("content"),
            inputs = arrayOf(null),
        ).asComposeRenderEffect()
    }
}
