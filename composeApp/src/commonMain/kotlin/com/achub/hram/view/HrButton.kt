package com.achub.hram.view

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.achub.hram.EventsCutter
import com.achub.hram.get
import com.achub.hram.style.Black
import com.achub.hram.style.DarkGray
import com.achub.hram.style.White20
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlin.time.ExperimentalTime

const val PRESS_ANIMATION_DURATION = 150L
val redDropShadowColor = Color(0x80FF0000)
val darkRedDropShadowColor = Color(0xCCFF0000)
val rippleColor = Color(0x40777777)

@Composable
fun <T> buttonPressAnimation() = tween<T>(
    durationMillis = PRESS_ANIMATION_DURATION.toInt(),
    easing = EaseInOut
)

@OptIn(FlowPreview::class, ExperimentalTime::class)
@Composable
fun HrButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean = true,
    content: @Composable BoxScope.(contentAlpha: Float) -> Unit
) {
    val eventsCutter = remember { EventsCutter.Companion.get(PRESS_ANIMATION_DURATION * 3) }
    val interactionSource = remember { MutableInteractionSource() }
    var clicked by remember { mutableStateOf(false) }
    val pressed by interactionSource.interactions.map { it is PressInteraction.Press }.debounce {
        PRESS_ANIMATION_DURATION
    }.collectAsStateWithLifecycle(false)
    LaunchedEffect(clicked, pressed) {
        if (!pressed) {
            delay(PRESS_ANIMATION_DURATION)
            clicked = pressed
        }
    }
    val transition = updateTransition(targetState = pressed || clicked, label = "button_press_transition")
    val buttonContentAlpha by transition.animateFloat(
        label = "buttonContentAlpha",
        transitionSpec = { buttonPressAnimation() }) { pressed ->
        if (pressed || !enabled) 0.3f else 0.6f
    }
    val shadowAlpha by transition.animateFloat(label = "shadowAlpha", transitionSpec = { buttonPressAnimation() }
    ) { pressed ->
        if (pressed || !enabled) 0.2f else 1f
    }
    val backgroundColor = if (enabled) DarkGray else White20
    val shape = RoundedCornerShape(16.dp)

    Box(
        Modifier.dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 8.dp,
                spread = 1.dp,
                color = redDropShadowColor,
                offset = DpOffset(x = 0.dp, -(1).dp),
                alpha = shadowAlpha
            )
        )
            .dropShadow(
                shape = shape,
                shadow = Shadow(
                    radius = 8.dp,
                    spread = 1.dp,
                    color = darkRedDropShadowColor,
                    offset = DpOffset(x = 2.dp, 3.dp),
                    alpha = shadowAlpha
                )
            )
            // note that the background needs to be defined before defining the inner shadow
            .background(
                color = Black,
                shape = shape
            ), contentAlignment = Center

    ) {
        Box(
            modifier = modifier.clip(shape)
                .clickable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = ripple(color = rippleColor)
                ) {
                    eventsCutter.processEvent {
                        clicked = true
                        onClick()
                    }
                }.border(2.dp, DarkGray, shape)
                .background(color = backgroundColor, shape = shape)
                .innerShadow(
                    shape = shape,
                    block = {
                        radius = 80f
                        color = Black
                    },
                ), contentAlignment = Center
        ) { content(buttonContentAlpha) }
    }
}