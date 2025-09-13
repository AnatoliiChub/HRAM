package com.achub.hram.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight.Companion.W700
import androidx.compose.ui.unit.dp
import com.achub.hram.format
import com.achub.hram.style.HeadingLarge
import com.achub.hram.style.Red
import com.achub.hram.style.White
import hram.composeapp.generated.resources.Res
import hram.composeapp.generated.resources.ic_distance
import hram.composeapp.generated.resources.ic_heart
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview


@Composable
fun HeartLabelRow(hrBpm: Int) {
    ImageLabelRow("$hrBpm BPM", Res.drawable.ic_heart, Red)
}

@Composable
fun DistanceLabelRow(distance: Float) {
    ImageLabelRow(distance.format(), Res.drawable.ic_distance)
}

@Composable
private fun ImageLabelRow(label: String, drawable: DrawableResource, color: Color = White) {
    Row {
        Image(
            modifier = Modifier.size(80.dp).padding(16.dp),
            imageVector = vectorResource(drawable),
            colorFilter = ColorFilter.tint(color),
            contentDescription = null
        )
        Text(
            modifier = Modifier.align(Alignment.CenterVertically),
            text = label,
            style = HeadingLarge.copy(color = White, fontWeight = W700)
        )
    }
}

@Composable
@Preview
fun PreviewImageLabelRow() {
    Column {
        HeartLabelRow(120)
        DistanceLabelRow(5.329f)
    }
}
