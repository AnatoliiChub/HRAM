package com.achub.hram.view.tabs

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import com.achub.hram.view.shader.liquidRipple
import kotlinx.coroutines.launch

@Composable
fun ProperLiquidBottomBar(
    selectedTabIndex: Int,
    onTabClick: suspend (Int) -> Unit
) {
    val scope = rememberCoroutineScope()
    var rippleCenter by remember { mutableStateOf(Offset.Unspecified) }
    var layoutPosition by remember { mutableStateOf(Offset.Zero) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { layoutPosition = it.positionInRoot() }
            .liquidRipple(center = rippleCenter)
    ) {
        MainTabType.entries.forEachIndexed { index, tab ->
            ImageTab(tab, index == selectedTabIndex) { offset ->
                rippleCenter = offset - layoutPosition
                scope.launch {
                    onTabClick(index)
                }
            }
        }
    }
}
