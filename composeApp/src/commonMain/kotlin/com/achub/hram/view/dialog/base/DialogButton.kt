package com.achub.hram.view.dialog.base

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.W600
import androidx.compose.ui.unit.dp
import com.achub.hram.style.LabelLarge
import com.achub.hram.style.White
import com.achub.hram.view.HrButton

@Composable
fun DialogButton(text: String, onClick: () -> Unit) {
    HrButton(
        modifier = Modifier.wrapContentSize(),
        onClick = onClick,
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            text = text,
            style = LabelLarge.copy(color = White, fontWeight = W600),
        )
    }
}
