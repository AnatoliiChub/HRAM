package com.achub.hram.view.shader

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.graphics.Shader
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asComposeRenderEffect

// Scale alpha by this factor then shift so the threshold sits at alpha ≈ 0.5
private const val GOOEY_ALPHA_SCALE = 18f

// Android ColorMatrix offsets are in the [0, 255] range: -9 * 255 = -2 295
private const val GOOEY_ALPHA_OFFSET = -2295f

@Composable
actual fun rememberGooeyRenderEffect(
    blurRadius: Float,
): androidx.compose.ui.graphics.RenderEffect? =
    remember(blurRadius) { buildGooeyEffect(blurRadius) }

private fun buildGooeyEffect(
    blurRadius: Float,
): androidx.compose.ui.graphics.RenderEffect {
    // 1. Gaussian blur merges nearby blob edges
    val blur = RenderEffect.createBlurEffect(blurRadius, blurRadius, Shader.TileMode.DECAL)

    // 2. Color-matrix that sharpens the alpha channel back to a hard edge:
    //    A_out = GOOEY_ALPHA_SCALE * A_in + GOOEY_ALPHA_OFFSET  (clamped to [0,255])
    //    RGB channels are left unchanged.
    val colorMatrix = ColorMatrix(
        floatArrayOf(
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
    )
    val colorFilterEffect = RenderEffect.createColorFilterEffect(
        ColorMatrixColorFilter(colorMatrix)
    )

    // Apply blur first (inner), then the color-matrix filter (outer)
    return RenderEffect.createChainEffect(colorFilterEffect, blur).asComposeRenderEffect()
}


