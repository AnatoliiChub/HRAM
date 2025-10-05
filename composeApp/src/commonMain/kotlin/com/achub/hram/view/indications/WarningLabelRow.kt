package com.achub.hram.view.indications

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.W400
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hram.composeapp.generated.resources.Res
import hram.composeapp.generated.resources.ic_warning

@Composable
fun WarningLabelRow(modifier: Modifier = Modifier, label: String) {
    ImageLabelRow(
        modifier = modifier,
        label = label,
        drawable = Res.drawable.ic_warning,
        imageSize = 48.dp,
        textSize = 18.sp,
        fontWeight = W400
    )
}