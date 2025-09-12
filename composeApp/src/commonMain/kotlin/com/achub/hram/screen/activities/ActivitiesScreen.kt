package com.achub.hram.screen.activities

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.achub.hram.style.LabelMedium
import hram.composeapp.generated.resources.Res
import hram.composeapp.generated.resources.ic_activities
import org.jetbrains.compose.resources.vectorResource

object ActivitiesScreen : Tab {

    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<ActivitiesViewModel>()
        val state = viewModel.uiState.value
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Companion.Center) {
            Text(text = state.title, style = LabelMedium)
        }
    }

    override val options: TabOptions
        @Composable
        get() {
            val title = "Activities"
            val icon = rememberVectorPainter(vectorResource(Res.drawable.ic_activities))

            return remember {
                TabOptions(
                    index = 0u,
                    title = title,
                    icon = icon
                )
            }
        }
}