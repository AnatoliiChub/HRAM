package com.achub.hram.view.tabs

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch

@Composable
fun MainTabLayout(
    selectedTabIndex: Int,
    pagerState: PagerState
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        MainTabType.entries.forEachIndexed { index, tab ->
            val scope = rememberCoroutineScope()
            ImageTab(tab, index == selectedTabIndex) {
                scope.launch {
                    pagerState.animateScrollToPage(index)
                }
            }
        }
    }
}
