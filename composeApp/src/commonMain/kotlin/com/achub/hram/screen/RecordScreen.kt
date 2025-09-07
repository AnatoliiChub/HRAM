package com.achub.hram.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.text.font.FontWeight.Companion.W700
import androidx.compose.ui.unit.dp
import com.achub.hram.style.Dimen16
import com.achub.hram.style.Heading1
import com.achub.hram.style.HeadingLarge
import com.achub.hram.style.Red
import com.achub.hram.style.White
import hram.composeapp.generated.resources.Res
import hram.composeapp.generated.resources.ic_distance
import hram.composeapp.generated.resources.ic_heart
import hram.composeapp.generated.resources.ic_pause
import hram.composeapp.generated.resources.ic_play
import hram.composeapp.generated.resources.ic_stop
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun RecordScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(top = 32.dp),
        horizontalAlignment = CenterHorizontally
    ) {
        Column {
            Row {
                Box(modifier = Modifier.size(80.dp), contentAlignment = Center) {
                    Image(
                        modifier = Modifier.size(48.dp),
                        imageVector = vectorResource(Res.drawable.ic_heart),
                        colorFilter = ColorFilter.tint(color = Red),
                        contentDescription = null
                    )
                }
                Spacer(modifier = Modifier.height(Dimen16))
                Text(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    text = "88",
                    style = HeadingLarge.copy(color = White, fontWeight = W700)
                )
            }
            Row {
                Box(modifier = Modifier.size(80.dp), contentAlignment = Center) {
                    Image(
                        modifier = Modifier.size(48.dp),
                        imageVector = vectorResource(Res.drawable.ic_distance),
                        colorFilter = ColorFilter.tint(color = White),
                        contentDescription = null
                    )
                }
                Spacer(modifier = Modifier.height(Dimen16))
                Text(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    text = "1.43 km",
                    style = HeadingLarge.copy(color = White, fontWeight = W700)
                )
            }
            Text(
                modifier = Modifier.align(CenterHorizontally),
                text = "15:53",
                style = Heading1.copy(color = White, fontWeight = W500)
            )
        }
        Spacer(Modifier.weight(1f))
        var isPlaying = remember { mutableStateOf(false) }
        RecordPanel(isPlaying.value, onPlay = { isPlaying.value = !isPlaying.value })
    }
}

@Composable
@Preview
fun RecordScreenPreview() {
    RecordScreen()
}

@Composable
fun RecordPanel(isPlaying: Boolean = false, onPlay: () -> Unit = {}, onStop: () -> Unit = {}) {

    Row(modifier = Modifier.padding(16.dp)) {
        val icon = if (isPlaying) Res.drawable.ic_pause else Res.drawable.ic_play
        val first by animateFloatAsState(if (!isPlaying) 0.5f else 1f)
        val second by animateFloatAsState(if (!isPlaying) 1f else 0f)
        FilledIconButton(
            modifier = Modifier.height(48.dp).fillMaxWidth(first).padding(end = 8.dp),
            onClick = onPlay,
            shape = RoundedCornerShape(8.dp),
            colors = IconButtonColors(Red, White, Red, White)
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null
            )
        }
        FilledIconButton(
            modifier = Modifier.height(48.dp).fillMaxWidth(second).padding(start = 8.dp),

            onClick = onStop,
            shape = RoundedCornerShape(8.dp),
            colors = IconButtonColors(Red, White, Red, White)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_stop),
                contentDescription = null
            )
        }


    }
}