package com.achub.hram.view.shader

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.unit.IntSize

/**
 * Returns a [RenderEffect] that draws two analytically-computed metaball circles
 * blended into a single gradient-filled shape with a fully transparent background.
 *
 * Unlike the blur-threshold approach, no Gaussian blur is applied, so composables
 * placed above this layer (e.g. icons) remain perfectly sharp.
 *
 * @param shaderSrc  AGSL / SkSL source loaded from the `.agsl` resource file.
 * @param center1    Centre of the first blob in **pixels** (origin = top-left of composable).
 * @param center2    Centre of the second blob in **pixels**.
 * @param radius     Blob radius in **pixels** — the iso-surface for an isolated circle.
 * @param topColor   Gradient colour at `y = 0` (top of the composable).
 * @param bottomColor Gradient colour at `y = resolution.height`.
 * @param resolution Composable size in pixels — used to normalise the gradient.
 */
@Composable
expect fun rememberMetaballRenderEffect(
    shaderSrc: String,
    center1: Offset,
    center2: Offset,
    radius: Float,
    topColor: Color,
    bottomColor: Color,
    borderColor: Color,
    resolution: IntSize,
): RenderEffect?
