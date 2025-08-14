package com.achub.hram

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
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
                .fillMaxSize()
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)

            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Center
                ) {
                    Text(
                        text = MainTab.entries[selectedTabIndex.value].name,
                        style = LabelMedium
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                MainTab.entries.forEachIndexed { index, tab ->
                    Column(
                        modifier = Modifier.weight(1f).padding(4.dp),
                        horizontalAlignment = CenterHorizontally,
                    ) {
                        Image(
                            modifier = Modifier.size(32.dp).clickable {
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            painter = painterResource(tab.icon),
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = tab.name, style = LabelMedium)
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
