package com.achub.hram.view.dialog.base

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
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.achub.hram.style.Black
import com.achub.hram.style.Dark
import com.achub.hram.style.DarkGray
import com.achub.hram.style.White
import org.jetbrains.compose.ui.tooling.preview.Preview

val Gradient = listOf(
    Color(0xFFE60000),
    Color(0xFFFF4D4D),
    Color(0xFFE60000),
    Color(0xFFFF4D4D),
    Color(0xFFE60000),
)
private val MIN_SPREAD = 1f
private val MAX_SPREAD = 4f
private val BLINKING_ANIMATION_DURATION = 3000

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

    val dropShadowShape = RoundedCornerShape(12.dp)
    val innerShadowShape = RoundedCornerShape(8.dp)

    Box(Modifier.padding(48.dp), contentAlignment = Center) {
        ElevatedCard(
            elevation = CardDefaults.cardElevation(24.dp),
            modifier = modifier.fillMaxWidth()
                .dropShadow(
                    shape = dropShadowShape,
                    shadow = Shadow(
                        radius = 8.dp,
                        spread = animatedSpread.dp,
                        brush = Brush.sweepGradient(Gradient),
                        offset = DpOffset(x = 0.dp, y = 0.dp),
                        alpha = if (animate) animatedSpread / 4f else 0f
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
                    .padding(2.dp)
                    .clip(innerShadowShape)
                    .background(color = DarkGray, shape = innerShadowShape)
                    .border(2.dp, Dark.copy(alpha = 0.3f), innerShadowShape)
                    .innerShadow(
                        shape = innerShadowShape,
                        block = {
                            radius = 80f
                            color = Black
                        },
                    ).clip(innerShadowShape),
                content = content
            )
        }
    }
}

@Composable
@Preview
fun DialogCardPreview() {
    Box(Modifier.fillMaxWidth()) {
        DialogElevatedCard(animate = true) {
            Text(
                modifier = Modifier.padding(16.dp).width(120.dp),
                text = "Short med assd d asasdd a d  da sa sd asd  dasas dda  dads a ads addad ada dasssage"
            )
        }
    }
}


private fun blinkingSpec(): KeyframesSpec<Float> {
    val second = (BLINKING_ANIMATION_DURATION * 0.5f).toInt()
    val third = (BLINKING_ANIMATION_DURATION * 1f).toInt()
    return keyframes {
        durationMillis = BLINKING_ANIMATION_DURATION
        MIN_SPREAD at 0 using LinearEasing
        MAX_SPREAD at second using LinearEasing
        MIN_SPREAD at third using LinearEasing
    }
}