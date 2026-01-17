package com.achub.hram.view.tabs

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.tooling.preview.Preview
import com.achub.hram.style.Dimen32
import com.achub.hram.style.Dimen4
import com.achub.hram.style.LabelMediumBold
import com.achub.hram.style.Red
import com.achub.hram.style.White
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun RowScope.ImageTab(
    tab: MainTabType,
    isSelected: Boolean,
    onTap: (Offset) -> Unit,
) {
    var tabRootPosition by remember { mutableStateOf(Offset.Unspecified) }
    Column(
        modifier = Modifier
            .weight(1f)
            .onGloballyPositioned { coordinates ->
                tabRootPosition = coordinates.positionInRoot()
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    if (tabRootPosition != Offset.Unspecified) {
                        onTap(tabRootPosition + offset)
                    }
                }
            }
            .padding(Dimen4),
        horizontalAlignment = CenterHorizontally,
    ) {
        val color = if (isSelected) Red else White
        Image(
            modifier = Modifier.size(Dimen32),
            imageVector = vectorResource(tab.icon),
            colorFilter = ColorFilter.tint(color = color),
            contentDescription = null
        )
        Spacer(modifier = Modifier.height(Dimen4))
        Text(text = stringResource(tab.label), style = LabelMediumBold.copy(color = color))
    }
}

@Preview
@Composable
private fun ImageTabPreview() {
    Row {
        ImageTab(
            tab = MainTabType.Activities,
            isSelected = true,
            onTap = {},
        )
    }
}
