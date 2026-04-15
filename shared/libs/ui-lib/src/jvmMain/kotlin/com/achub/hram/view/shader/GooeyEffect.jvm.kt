package com.achub.hram.view.shader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.asComposeRenderEffect
import org.jetbrains.skia.ColorFilter
import org.jetbrains.skia.ColorMatrix
import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.ImageFilter

// Desktop (JVM/Skiko) — mirrors the iOS/Skia implementation.
private const val GOOEY_ALPHA_SCALE = 18f
private const val GOOEY_ALPHA_OFFSET = -9f

@Composable
actual fun rememberGooeyRenderEffect(blurRadius: Float): RenderEffect? =
    remember(blurRadius) {
        val blur = ImageFilter.makeBlur(blurRadius, blurRadius, FilterTileMode.DECAL)
        val colorMatrix = ColorMatrix(
            1f,
            0f,
            0f,
            0f,
            0f,
            0f,
            1f,
            0f,
            0f,
            0f,
            0f,
            0f,
            1f,
            0f,
            0f,
            0f,
            0f,
            0f,
            GOOEY_ALPHA_SCALE,
            GOOEY_ALPHA_OFFSET,
        )
        val colorFilter = ColorFilter.makeMatrix(colorMatrix)
        ImageFilter.makeColorFilter(colorFilter, blur, null).asComposeRenderEffect()
    }
