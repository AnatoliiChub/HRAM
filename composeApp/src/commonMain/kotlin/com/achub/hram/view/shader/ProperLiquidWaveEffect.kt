package com.achub.hram.view.shader

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.achub.hram.style.DarkRed

// language=AGSL
const val LIQUID_SHADER_SRC = """
uniform shader content;
uniform float2 resolution;
uniform float time;
uniform float density;
uniform float minRadius;      // min radius in pixels
uniform float2 origin;        // normalized origin (0..1)
layout(color) uniform half4 color; // base tint

half4 main(float2 coord) {
    float2 center = resolution * origin;
    float2 pos = coord - center;
    float dist = length(pos);
    float theta = atan(pos.y, pos.x);

    // Max radius: ~half smaller screen dimension
    float maxRadius = min(resolution.x, resolution.y) / 2.2;

    half4 accColor = half4(0.0);
    float totalDisplacement = 0.0;

    // Accumulate 4 repeating rings for liquid depth
    for (float i = 0.0; i < 4.0; i += 1.0) {
        float offset = i / 4.0;
        float p = mod(time + offset, 1.0);

        // 1. Chaos/Noise Calculation
        float waveOffset = sin(theta * 6.0 + p * 8.0) + (cos(theta * 10.0 - p * 12.0) * 0.5);
        float chaosAmp = (9.0 * density) * p;

        // 2. Distorted Radius
        // Scale p so that radius goes from minRadius to maxRadius
        float effectiveMax = max(maxRadius, minRadius + 0.001);
        float startR = minRadius;
        float currentTargetR = startR + (effectiveMax - startR) * p;

        float r = currentTargetR + (waveOffset * chaosAmp);
        float d = abs(dist - r);

        // 3. Fade Logic
        float startFade = 0.0;
        float fadeIn = clamp((p - startFade) / 0.2, 0.0, 1.0);
        float fadeOut = clamp(1.0 - p, 0.0, 1.0);
        float a = fadeIn * fadeOut;

        if (a <= 0.001) continue;

        // 4. Liquid Body Expansion
        float expansion = (4.0 * density) + (36.0 * density * p);

        // 5. Draw Liquid
        float normD = d / expansion;
        
        // Calculate the base gradient (0.0 at edge, 1.0 at center)
        float baseBody = smoothstep(1.0, 0.0, normD);
        float bodyAlpha = pow(baseBody, 5.0);
        float coreAlpha = smoothstep(1.0, 0.0, normD);

        accColor += color * (bodyAlpha * 0.2 + coreAlpha * 0.2) * a;
        
        // 6. Draw Glow/Ring
        // 'd' is the absolute distance in pixels from the ripple center
        // We want a glow that is, say, 20 pixels wide (scaled by density)
        float glowWidth = 62.0 * density;

        // 1. Calculate the base linear falloff (0.0 to 1.0)
        float baseGlow = 1.0 - smoothstep(0.0, glowWidth, d);
        
        // 2. THE FIX: Raise it to a power to make sides transparent faster
        // 1.0 = Linear (What you have now)
        // 2.0 = Soft Quadratic fade
        // 4.0 = Very sharp center, barely visible sides (Gas/Fire look)
        float glowStrength = pow(baseGlow, 3.0);

        // Accumulate the glow
        // Multiply by 'alpha' so it fades out with the ring
        accColor += color * glowStrength * a ;

        // 7. Accumulate Displacement (based on wave body presence)
        totalDisplacement += bodyAlpha * a;
    }

    // Calculate displacement vector
    float2 dir = dist > 0.0 ? normalize(pos) : float2(0.0, 0.0);
    // Strength factor 
    float2 disp = dir * totalDisplacement * 4.0 * density;
    
    // Sample content with displacement
    half4 c = content.eval(coord - disp);

    // Composite additive sonar light over the content
    return c + accColor;
}
"""

@Composable
expect fun rememberLiquidRenderEffect(
    shaderSrc: String = LIQUID_SHADER_SRC,
    time: Float,
    density: Float,
    center: Offset = Offset.Unspecified,
    baseColor: Color,
    resolution: IntSize = IntSize.Zero,
    minRadius: Float,
): RenderEffect?

@Composable
fun ProperLiquidWaveEffect(
    baseColor: Color = DarkRed,
    durationMillis: Int = 4000,
    apply: Boolean,
    center: Offset = Offset.Unspecified,
    minRadius: Dp = 0.dp,
    content: @Composable BoxScope.() -> Unit
) {
    // Track size so we can provide rects to the shader and center calculations if needed
    var size by remember { mutableStateOf(IntSize.Zero) }

    val density = LocalDensity.current.density

    // Normalized animated time [0f..1f)
    val infinite = rememberInfiniteTransition()
    val animatedProgress by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween<Float>(durationMillis = durationMillis, easing = LinearEasing))
    )

    val minRadiusPx = with(LocalDensity.current) { minRadius.toPx() }

    // Apply the renderEffect to the content layer so the shader modifies the content (liquid/distortion)
    Box(
        modifier = Modifier
            .onGloballyPositioned { coords ->
                size = coords.size
            }
            .fillMaxSize()
    ) {
        val effect = rememberLiquidRenderEffect(
            shaderSrc = LIQUID_SHADER_SRC,
            time = animatedProgress,
            density = density,
            center = if (center != Offset.Unspecified) center else Offset(size.width / 2f, size.height / 2f),
            baseColor = baseColor,
            resolution = size,
            minRadius = minRadiusPx
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // Only set renderEffect when visible; graphicsLayer accepts null
                    this.renderEffect = if (apply) effect else null
                    this.alpha = alpha
                }
        ) {
            content()
        }
    }
}
