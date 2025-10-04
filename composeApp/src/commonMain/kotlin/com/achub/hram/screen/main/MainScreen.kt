package com.achub.hram.screen.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.achub.hram.screen.activities.ActivitiesScreen
import com.achub.hram.screen.record.RecordScreen
import com.achub.hram.style.Black
import com.achub.hram.view.MainTabLayout
import com.achub.hram.view.TabType
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MainScreen() {
    MaterialTheme {
        val pagerState = rememberPagerState(initialPage = 0, pageCount = { TabType.entries.size })
        Column(
            modifier = Modifier
                .background(color = Black)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .fillMaxSize()
        ) {
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = true,
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) { index ->
                when (TabType.entries[index]) {
                    TabType.Activities -> ActivitiesScreen()
                    TabType.Record -> RecordScreen()
                }
            }
            MainTabRow(pagerState.currentPage, pagerState)
        }
    }
}

@Composable
private fun MainTabRow(
    selectedTabIndex: Int,
    pagerState: PagerState
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        TabType.entries.forEachIndexed { index, tab ->
            val scope = rememberCoroutineScope()
            MainTabLayout(tab, index == selectedTabIndex) {
                scope.launch {
                    pagerState.animateScrollToPage(index)
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
