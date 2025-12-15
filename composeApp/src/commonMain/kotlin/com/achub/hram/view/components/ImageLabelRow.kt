package com.achub.hram.view.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.achub.hram.style.Dimen64
import com.achub.hram.style.HeadingMediumBold
import com.achub.hram.style.Red
import hram.composeapp.generated.resources.Res
import hram.composeapp.generated.resources.ic_distance
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.vectorResource

private const val IMAGE_PADDING_DIVISOR = 10

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
        modifier = modifier.size(imageSize).padding(imageSize / IMAGE_PADDING_DIVISOR),
        imageVector = vectorResource(drawable),
        colorFilter = color?.let { ColorFilter.tint(it) },
        contentDescription = null
    )
}

@Preview
@Composable
private fun ImageLabelRowPreview() {
    ImageLabelRow(
        label = "5.34 km",
        drawable = Res.drawable.ic_distance,
        color = Red,
    )
}
