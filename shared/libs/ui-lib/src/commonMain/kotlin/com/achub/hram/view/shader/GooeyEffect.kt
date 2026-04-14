package com.achub.hram.view.shader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Returns a [RenderEffect] that applies a Gaussian blur followed by a sharp alpha
 * threshold, producing the classic gooey / metaball merging effect.
 *
 * @param blurRadius Blur sigma in **pixels**.
 */
@Composable
expect fun rememberGooeyRenderEffect(blurRadius: Float): RenderEffect?

/**
 * A [Box] that renders its children into an offscreen layer and applies the gooey
 * metaball render effect (blur + alpha contrast) to that layer.
 *
 * Place pill-shaped composables inside and animate their positions: when two blobs
 * are close enough they appear to merge, and when they move apart they split.
 */
@Composable
fun GooeyContainer(
    modifier: Modifier = Modifier,
    blurRadius: Dp = 12.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val blurPx = with(LocalDensity.current) { blurRadius.toPx() }
    val effect = rememberGooeyRenderEffect(blurPx)
    Box(
        modifier = modifier.graphicsLayer {
            renderEffect = effect
            compositingStrategy =
                if (effect != null) CompositingStrategy.Offscreen else CompositingStrategy.Auto
        },
        content = content,
    )
}

