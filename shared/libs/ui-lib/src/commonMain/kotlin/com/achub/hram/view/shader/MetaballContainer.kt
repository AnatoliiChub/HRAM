package com.achub.hram.view.shader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import com.achub.hram.view.shader as loadShader

/**
 * A [Box] that renders a two-circle metaball shape using an AGSL/SkSL shader.
 *
 * The metaball is drawn analytically (no Gaussian blur) against a fully transparent
 * background, so any composables placed **above** this layer remain perfectly sharp.
 *
 * Composables placed **inside** ([content]) are rendered into the same offscreen
 * layer — use this slot for invisible click-target Boxes so touch events still reach
 * the correct button area (touch handling is unaffected by the renderEffect).
 *
 * @param center1     Centre of the first blob in **pixels** (origin = composable top-left).
 * @param center2     Centre of the second blob in **pixels**.
 * @param radius      Blob radius in **pixels**.
 * @param topColor    Gradient highlight colour (top of the container).
 * @param bottomColor Gradient shadow colour (bottom of the container).
 * @param borderColor Stroke colour drawn around the metaball boundary. Defaults to [Color.White].
 */
@Composable
fun MetaballContainer(
    modifier: Modifier = Modifier,
    center1: Offset,
    center2: Offset,
    radius: Float,
    topColor: Color,
    bottomColor: Color,
    borderColor: Color,
    content: @Composable BoxScope.() -> Unit = {},
) {
    var size by remember { mutableStateOf(IntSize.Zero) }

    // Load AGSL source from compose resources (suspends until ready; null on first frame).
    val shaderSrc = loadShader("files/shaders/metaball.agsl")

    val effect = shaderSrc?.let { src ->
        rememberMetaballRenderEffect(
            shaderSrc = src,
            center1 = center1,
            center2 = center2,
            radius = radius,
            topColor = topColor,
            bottomColor = bottomColor,
            borderColor = borderColor,
            resolution = size,
        )
    }

    Box(
        modifier = modifier
            .onGloballyPositioned { coords -> size = coords.size }
            .graphicsLayer {
                renderEffect = effect
                // Offscreen compositing is required when a renderEffect is active so the
                // shader can read the composable's rendered pixels via the `content` sampler.
                compositingStrategy = if (effect != null) {
                    CompositingStrategy.Offscreen
                } else {
                    CompositingStrategy.Auto
                }
            },
        content = content,
    )
}
