package com.achub.hram.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.achub.hram.style.Black
import com.achub.hram.view.MainTab
import com.achub.hram.view.MainTabLayout
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MainScreen() {
    MaterialTheme {
        val scope = rememberCoroutineScope()
        val pagerState = rememberPagerState(pageCount = { MainTab.entries.size })
        val selectedTabIndex = remember { derivedStateOf { pagerState.currentPage } }

        Column(
            modifier = Modifier
                .background(color = Black)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .fillMaxSize()
        ) {
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false,
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                when (MainTab.entries[selectedTabIndex.value]) {
                    MainTab.Activities -> ActivitiesScreen()
                    MainTab.Record -> RecordScreen()
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                MainTab.entries.forEachIndexed { index, tab ->
                    MainTabLayout(tab, index == selectedTabIndex.value) {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun MainScreenPreview() {
    MainScreen()
}
