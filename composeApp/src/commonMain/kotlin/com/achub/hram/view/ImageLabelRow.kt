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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.W400
import androidx.compose.ui.text.font.FontWeight.Companion.W700
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.achub.hram.format
import com.achub.hram.style.Red
import com.achub.hram.style.White
import hram.composeapp.generated.resources.Res
import hram.composeapp.generated.resources.ic_distance
import hram.composeapp.generated.resources.ic_heart
import hram.composeapp.generated.resources.ic_warning
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview


@Composable
fun HeartLabelRow(hrBpm: Int) {
    ImageLabelRow(label = "$hrBpm BPM", drawable = Res.drawable.ic_heart, color = Red)
}

@Composable
fun DistanceLabelRow(distance: Float) {
    ImageLabelRow(label = "${distance.format()} km", drawable = Res.drawable.ic_distance)
}

@Composable
fun WarningLabelRow(label: String) {
    ImageLabelRow(
        label = label, drawable = Res.drawable.ic_warning, imageSize = 48.dp, textSize = 20.sp, fontWeight = W400
    )
}

@Composable
private fun ImageLabelRow(
    label: String,
    drawable: DrawableResource,
    color: Color? = null,
    imageSize: Dp = 80.dp,
    textSize: TextUnit = 36.sp,
    fontWeight: FontWeight = W700
) {
    Row {
        Image(
            modifier = Modifier.size(imageSize).padding(imageSize / 5),
            imageVector = vectorResource(drawable),
            colorFilter = color?.let { ColorFilter.tint(it) },
            contentDescription = null
        )
        Text(
            modifier = Modifier.align(Alignment.CenterVertically),
            text = label,
            style = TextStyle(color = White, fontWeight = fontWeight, fontSize = textSize)
        )
    }
}

@Composable
@Preview
fun PreviewImageLabelRow() {
    Column {
        HeartLabelRow(120)
        DistanceLabelRow(5.329f)
        WarningLabelRow("Choose at least one tracking option")
    }
}
