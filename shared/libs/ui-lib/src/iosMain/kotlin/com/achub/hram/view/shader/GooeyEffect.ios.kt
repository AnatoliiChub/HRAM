package com.achub.hram.view.shader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.asComposeRenderEffect
import org.jetbrains.skia.ColorFilter
import org.jetbrains.skia.ColorMatrix
import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.ImageFilter

// Skia color values are normalised [0, 1].
// Scale alpha by 18 and shift so the threshold sits at alpha ≈ 0.5:
//   A_out = 18 * A_in - 9
private const val GOOEY_ALPHA_SCALE = 18f
private const val GOOEY_ALPHA_OFFSET = -9f

@Composable
actual fun rememberGooeyRenderEffect(blurRadius: Float): RenderEffect? =
    remember(blurRadius) {
        // 1. Blur to merge nearby blob edges
        val blur = ImageFilter.makeBlur(blurRadius, blurRadius, FilterTileMode.DECAL)

        // 2. Color matrix: keep RGB, sharpen alpha back to a hard edge.
        //    4 × 5 row-major matrix (R, G, B, A rows; 5 columns = r,g,b,a,const).
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

        // Apply blur first (inner), then the color-filter (outer)
        ImageFilter.makeColorFilter(colorFilter, blur, null).asComposeRenderEffect()
    }
