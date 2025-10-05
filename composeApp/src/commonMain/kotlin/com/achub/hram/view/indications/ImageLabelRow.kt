package com.achub.hram.view.indications

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.W700
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.achub.hram.data.model.HrNotifications
import com.achub.hram.style.White
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ImageLabelRow(
    modifier: Modifier = Modifier,
    label: String,
    drawable: DrawableResource,
    color: Color? = null,
    imageSize: Dp = 80.dp,
    textSize: TextUnit = 36.sp,
    fontWeight: FontWeight = W700
) {
    Row(modifier) {
        IndicationImage(imageSize = imageSize, drawable = drawable, color = color)
        Text(
            modifier = Modifier.align(CenterVertically),
            text = label,
            style = TextStyle(color = White, fontWeight = fontWeight, fontSize = textSize)
        )
    }
}

@Composable
fun IndicationImage(
    modifier: Modifier = Modifier,
    imageSize: Dp,
    drawable: DrawableResource,
    color: Color?
) {
    Image(
        modifier = modifier.size(imageSize).padding(imageSize / 5),
        imageVector = vectorResource(drawable),
        colorFilter = color?.let { ColorFilter.tint(it) },
        contentDescription = null
    )
}

@Composable
@Preview
fun PreviewImageLabelRow() {
    Column(Modifier.fillMaxWidth()) {
        HeartLabelRow(hrNotifications = HrNotifications(hrBpm = 180, batteryLevel = 76))
        DistanceLabelRow(distance = 5.329f)
        WarningLabelRow(label = "Choose at least one tracking option")
    }
}

