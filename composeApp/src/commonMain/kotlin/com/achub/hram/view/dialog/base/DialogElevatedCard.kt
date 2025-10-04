package com.achub.hram.view.dialog.base

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.achub.hram.style.White

@Composable
fun DialogElevatedCard(backgroundCardColor: Color, content: @Composable (ColumnScope) -> Unit) {
    ElevatedCard(
        elevation = CardDefaults.cardElevation(16.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
        colors = CardColors(
            contentColor = White,
            containerColor = backgroundCardColor,
            disabledContainerColor = backgroundCardColor,
            disabledContentColor = White
        ),
        content = content
    )
}