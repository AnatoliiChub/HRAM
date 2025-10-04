package com.achub.hram.screen.activities

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.achub.hram.style.LabelMedium
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ActivitiesScreen() {
    val viewModel = koinViewModel<ActivitiesViewModel>()
    val state = viewModel.uiState.value
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Companion.Center) {
        Text(text = state.title, style = LabelMedium)
    }
}
