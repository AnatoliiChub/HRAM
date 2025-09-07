package com.achub.hram.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.unit.dp
import com.achub.hram.style.Heading1
import com.achub.hram.style.Red
import com.achub.hram.style.White
import com.achub.hram.view.ImageLabelRow
import com.achub.hram.view.RecordRow
import hram.composeapp.generated.resources.Res
import hram.composeapp.generated.resources.ic_distance
import hram.composeapp.generated.resources.ic_heart
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun RecordScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(top = 32.dp),
        horizontalAlignment = CenterHorizontally
    ) {
        val isPlaying = remember { mutableStateOf(false) }

        Column {
            ImageLabelRow("88", Res.drawable.ic_heart, )
            ImageLabelRow("1.43 km", Res.drawable.ic_distance, Red)
            Text(
                modifier = Modifier.align(CenterHorizontally),
                text = "15:53",
                style = Heading1.copy(color = White, fontWeight = W500)
            )
        }
        Spacer(Modifier.weight(1f))
        RecordRow(isPlaying.value, onPlay = { isPlaying.value = !isPlaying.value })
    }
}

@Composable
@Preview
fun RecordScreenPreview() {
    RecordScreen()
}
