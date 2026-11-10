package com.achub.hram.screen.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.achub.hram.screen.activities.ActivitiesScreen
import com.achub.hram.screen.record.RecordScreen
import com.achub.hram.screen.settings.SettingsScreen
import com.achub.hram.style.HramTheme
import com.achub.hram.view.tabs.ProperLiquidBottomBar
import org.koin.compose.viewmodel.koinViewModel

private val tabs = listOf(Record, Activities, Settings)

@Composable
fun MainScreen() {
    val viewModel = koinViewModel<MainViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val darkTheme = when (state.theme) {
        com.achub.hram.models.AppTheme.System -> isSystemInDarkTheme()
        com.achub.hram.models.AppTheme.Dark -> true
        com.achub.hram.models.AppTheme.Light -> false
    }

    ApplyStatusBarStyle(darkTheme)

    HramTheme(darkTheme = darkTheme) {
        val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabs.size })
        Column(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.background)
                .fillMaxSize()
        ) {
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .then(
                        if (tabs[pagerState.currentPage] is Activities) {
                            Modifier
                        } else {
                            Modifier.windowInsetsPadding(WindowInsets.statusBars)
                        }
                    )
            ) { index ->
                when (tabs[index]) {
                    is Activities -> ActivitiesScreen()
                    is Record -> RecordScreen()
                    is Settings -> SettingsScreen()
                }
            }
            Column(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)) {
                ProperLiquidBottomBar(tabs, pagerState.currentPage, pagerState::animateScrollToPage)
            }
        }
    }
}

@Composable
@Preview
fun MainScreenPreview() {
    MainScreen()
}
