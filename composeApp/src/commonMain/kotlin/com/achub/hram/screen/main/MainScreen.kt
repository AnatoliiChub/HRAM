package com.achub.hram.screen.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.achub.hram.screen.activities.ActivitiesScreen
import com.achub.hram.screen.record.RecordScreen
import com.achub.hram.style.Black
import com.achub.hram.view.tabs.MainTabType
import com.achub.hram.view.tabs.ProperLiquidBottomBar

@Composable
fun MainScreen() {
    MaterialTheme {
        val pagerState = rememberPagerState(initialPage = 0, pageCount = { MainTabType.entries.size })
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
            ) { index ->
                when (MainTabType.entries[index]) {
                    MainTabType.Activities -> ActivitiesScreen()
                    MainTabType.Record -> RecordScreen()
                }
            }
            ProperLiquidBottomBar(pagerState.currentPage, pagerState::animateScrollToPage)
        }
    }
}

@Composable
@Preview
fun MainScreenPreview() {
    MainScreen()
}
