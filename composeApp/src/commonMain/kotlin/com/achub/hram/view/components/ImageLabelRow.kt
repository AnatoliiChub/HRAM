package com.achub.hram.view.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.achub.hram.data.models.HrIndication
import com.achub.hram.style.Dimen64
import com.achub.hram.style.HeadingMediumBold
import com.achub.hram.view.indications.DistanceLabelRow
import com.achub.hram.view.indications.HeartLabelRow
import com.achub.hram.view.indications.WarningLabelRow
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun ImageLabelRow(
    modifier: Modifier = Modifier,
    label: String,
    drawable: DrawableResource,
    color: Color? = null,
    imageSize: Dp = Dimen64,
    textStyle: TextStyle = HeadingMediumBold,
) {
    Row(modifier) {
        IndicationImage(imageSize = imageSize, drawable = drawable, color = color)
        Text(modifier = Modifier.align(CenterVertically), text = label, style = textStyle)
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
        modifier = modifier.size(imageSize).padding(imageSize / 10),
        imageVector = vectorResource(drawable),
        colorFilter = color?.let { ColorFilter.tint(it) },
        contentDescription = null
    )
}

@Composable
@Preview
fun PreviewImageLabelRow() {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = CenterHorizontally) {
        HeartLabelRow(hrIndication = HrIndication(hrBpm = 80, batteryLevel = 76))
        DistanceLabelRow(distance = 5.329f)
        WarningLabelRow(label = "Choose at least one tracking option")
    }
}

