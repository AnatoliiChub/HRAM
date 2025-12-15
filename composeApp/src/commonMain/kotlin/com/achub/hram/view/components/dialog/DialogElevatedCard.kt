package com.achub.hram.view.components.dialog

import androidx.compose.animation.core.KeyframesSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.achub.hram.style.Black
import com.achub.hram.style.Dark
import com.achub.hram.style.DarkGray
import com.achub.hram.style.Dimen12
import com.achub.hram.style.Dimen120
import com.achub.hram.style.Dimen16
import com.achub.hram.style.Dimen2
import com.achub.hram.style.Dimen24
import com.achub.hram.style.Dimen48
import com.achub.hram.style.Dimen8
import com.achub.hram.style.GradientLightRed
import com.achub.hram.style.GradientRed
import com.achub.hram.style.White

val Gradient = listOf(
    GradientRed,
    GradientLightRed,
    GradientRed,
    GradientLightRed,
    GradientRed,
)

private const val MIN_SPREAD = 1f
private const val MAX_SPREAD = 4f
private const val BLINKING_ANIMATION_DURATION = 3000

// extracted magic numbers
private const val INNER_SHADOW_RADIUS = 80f
private const val BORDER_ALPHA = 0.3f
private const val SPREAD_ALPHA_DIVISOR = 4f
private const val SECOND_KEYFRAME_RATIO = 0.5f

@Composable
fun DialogElevatedCard(
    modifier: Modifier = Modifier,
    backgroundCardColor: Color = DarkGray,
    animate: Boolean = false,
    content: @Composable (ColumnScope) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val animatedSpread by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = blinkingSpec(),
            repeatMode = RepeatMode.Restart
        )
    )

    val dropShadowShape = RoundedCornerShape(Dimen12)
    val innerShadowShape = RoundedCornerShape(Dimen8)

    Box(Modifier.padding(vertical = Dimen48), contentAlignment = Center) {
        ElevatedCard(
            elevation = CardDefaults.cardElevation(Dimen24),
            modifier = modifier.fillMaxWidth()
                .dropShadow(
                    shape = dropShadowShape,
                    shadow = Shadow(
                        radius = Dimen8,
                        spread = animatedSpread.dp,
                        brush = Brush.sweepGradient(Gradient),
                        alpha = if (animate) animatedSpread / SPREAD_ALPHA_DIVISOR else 0f
                    )
                ).clip(dropShadowShape),
            colors = CardColors(
                contentColor = White,
                containerColor = backgroundCardColor,
                disabledContainerColor = backgroundCardColor,
                disabledContentColor = White
            ),
        ) {
            Column(
                modifier.fillMaxWidth()
                    .padding(Dimen2)
                    .clip(innerShadowShape)
                    .background(color = DarkGray, shape = innerShadowShape)
                    .border(Dimen2, Dark.copy(alpha = BORDER_ALPHA), innerShadowShape)
                    .innerShadow(
                        shape = innerShadowShape,
                        block = {
                            radius = INNER_SHADOW_RADIUS
                            color = Black
                        },
                    ).clip(innerShadowShape),
                content = content
            )
        }
    }
}

private fun blinkingSpec(): KeyframesSpec<Float> {
    val second = (BLINKING_ANIMATION_DURATION * SECOND_KEYFRAME_RATIO).toInt()
    return keyframes {
        durationMillis = BLINKING_ANIMATION_DURATION
        MIN_SPREAD at 0 using LinearEasing
        MAX_SPREAD at second using LinearEasing
        MIN_SPREAD at BLINKING_ANIMATION_DURATION using LinearEasing
    }
}

@Composable
@Preview
fun DialogCardPreview() {
    Box(Modifier.fillMaxWidth()) {
        DialogElevatedCard(animate = true) {
            Text(
                modifier = Modifier.padding(Dimen16).width(Dimen120),
                text = "Short med assd d asasdd a d  da sa sd asd  dasas dda  dads a ads addad ada dasssage"
            )
        }
    }
}
