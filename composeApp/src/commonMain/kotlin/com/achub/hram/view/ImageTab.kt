package com.achub.hram.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import com.achub.hram.style.Dimen4
import com.achub.hram.style.LabelMedium
import com.achub.hram.style.Red
import com.achub.hram.style.White
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.vectorResource

@Composable

fun MainTabLayout(
    selectedTabIndex: Int,
    onClick: (Int) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        TabType.entries.forEachIndexed { index, tab ->
            val scope = rememberCoroutineScope()
            MainTabLayout(tab, index == selectedTabIndex) { scope.launch { onClick(index) } }
        }
    }
}

@Composable
private fun RowScope.MainTabLayout(
    tab: TabType,
    isSelected: Boolean,
    onTap: () -> Unit,
) {
    Column(
        modifier = Modifier.weight(1f).padding(Dimen4),
        horizontalAlignment = CenterHorizontally,
    ) {
        val color = if (isSelected) Red else White
        Image(
            modifier = Modifier.size(32.dp).clickable(onClick = onTap),
            imageVector = vectorResource(tab.icon),
            colorFilter = ColorFilter.tint(color = color),
            contentDescription = null
        )
        Spacer(modifier = Modifier.height(Dimen4))
        Text(text = tab.name, style = LabelMedium.copy(color = color))
    }
}