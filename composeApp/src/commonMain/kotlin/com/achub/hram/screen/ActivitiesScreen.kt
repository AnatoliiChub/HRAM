package com.achub.hram.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import com.achub.hram.style.LabelMedium
import com.achub.hram.view.TabType

@Composable
fun ActivitiesScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Center
    ) {
        Text(
            text = TabType.Activities.name,
            style = LabelMedium
        )
    }
}