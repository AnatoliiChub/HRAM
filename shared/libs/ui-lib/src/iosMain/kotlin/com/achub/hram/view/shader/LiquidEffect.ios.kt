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

@Composable
actual fun rememberLiquidRenderEffect(
    shaderSrc: String,
    time: Float,
    density: Float,
    center: Offset,
    baseColor: Color,
    resolution: IntSize,
    minRadius: Float
): RenderEffect? {
    val runtimeEffect = remember(shaderSrc) { RuntimeEffect.makeForShader(shaderSrc) }

    return remember(runtimeEffect, time, density, center, baseColor, resolution, minRadius) {
        val builder = RuntimeShaderBuilder(runtimeEffect)

        val resX = if (resolution.width > 0) resolution.width.toFloat() else 0f
        val resY = if (resolution.height > 0) resolution.height.toFloat() else 0f
        builder.uniform("resolution", resX, resY)
        builder.uniform("time", time)
        builder.uniform("density", density)
        builder.uniform("minRadius", minRadius)

        val originX = if (center != Offset.Unspecified) {
            if (resX > 0f) {
                center.x / resX
            } else {
                0.5f
            }
        } else {
            0.5f
        }
        val originY = if (center != Offset.Unspecified) {
            if (resY > 0f) {
                center.y / resY
            } else {
                0.5f
            }
        } else {
            0.5f
        }
        builder.uniform("origin", originX, originY)

        builder.uniform("color", baseColor.red, baseColor.green, baseColor.blue, baseColor.alpha)

        ImageFilter.makeRuntimeShader(
            runtimeShaderBuilder = builder,
            shaderNames = arrayOf("content"),
            inputs = arrayOf(null)
        ).asComposeRenderEffect()
    }
}
