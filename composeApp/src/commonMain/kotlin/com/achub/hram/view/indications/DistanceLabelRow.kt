package com.achub.hram.view.indications

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.achub.hram.ext.format
import com.achub.hram.view.components.ImageLabelRow
import hram.composeapp.generated.resources.Res
import hram.composeapp.generated.resources.ic_distance

@Composable
fun DistanceLabelRow(modifier: Modifier = Modifier, distance: Float) {
    ImageLabelRow(modifier = modifier, label = "${distance.format()} km", drawable = Res.drawable.ic_distance)
}

@Preview
@Composable
private fun DistanceLabelRowPreview() {
    DistanceLabelRow(distance = 5.34f)
}
