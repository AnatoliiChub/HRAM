package com.achub.hram.view.indications

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.achub.hram.style.Dimen48
import com.achub.hram.style.LabelMedium
import com.achub.hram.view.components.ImageLabelRow
import hram.composeapp.generated.resources.Res
import hram.composeapp.generated.resources.ic_warning
import hram.composeapp.generated.resources.record_scree_choose_at_least_one_option
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun WarningLabelRow(modifier: Modifier = Modifier, label: StringResource) {
    ImageLabelRow(
        modifier = modifier,
        label = stringResource(label),
        drawable = Res.drawable.ic_warning,
        imageSize = Dimen48,
        textStyle = LabelMedium
    )
}

@Preview
@Composable
private fun WarningLabelRowPreview() {
    WarningLabelRow(label = Res.string.record_scree_choose_at_least_one_option)
}
