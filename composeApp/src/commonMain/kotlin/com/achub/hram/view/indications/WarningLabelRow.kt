package com.achub.hram.view.indications

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.achub.hram.style.Dimen48
import com.achub.hram.style.LabelMedium
import com.achub.hram.view.components.ImageLabelRow
import hram.composeapp.generated.resources.Res
import hram.composeapp.generated.resources.ic_warning

@Composable
fun WarningLabelRow(modifier: Modifier = Modifier, label: String) {
    ImageLabelRow(
        modifier = modifier,
        label = label,
        drawable = Res.drawable.ic_warning,
        imageSize = Dimen48,
        textStyle = LabelMedium
    )
}