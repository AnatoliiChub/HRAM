package com.achub.hram.screen.activities

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.achub.hram.view.ActivityCard
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ActivitiesScreen() {
    val viewModel = koinViewModel<ActivitiesViewModel>()
    val state = viewModel.uiState.collectAsStateWithLifecycle()
    val list = state.value.list
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Companion.Center) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items = list, key = { it.activity.id }) { activityInfo ->
                ActivityCard(activityInfo)
            }
        }
    }
}
