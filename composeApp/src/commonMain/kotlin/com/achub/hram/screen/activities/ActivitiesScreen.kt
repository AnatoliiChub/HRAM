package com.achub.hram.screen.activities

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.achub.hram.style.Dimen8
import com.achub.hram.view.ActivityCard
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ActivitiesScreen() {
    val viewModel = koinViewModel<ActivitiesViewModel>()
    with(viewModel.uiState.collectAsStateWithLifecycle().value) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Center) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimen8),
                verticalArrangement = Arrangement.spacedBy(Dimen8)
            ) {
                items(items = list, key = { it.activity.id }) { activityInfo ->
                    ActivityCard(
                        activityInfo,
                        highLighted = highlightedItem,
                        onHighlighted = { viewModel.onHighlighted(it) })
                }
            }
        }
    }
}
