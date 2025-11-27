package com.achub.hram.view.components.dialog

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.W600
import androidx.compose.ui.text.style.TextAlign
import com.achub.hram.style.LabelMedium
import com.achub.hram.style.White
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun DialogTitle(modifier: Modifier = Modifier, title: StringResource) {
    Text(
        modifier = modifier,
        text = stringResource(title),
        style = LabelMedium.copy(color = White, fontWeight = W600),
        textAlign = TextAlign.Center
    )
}