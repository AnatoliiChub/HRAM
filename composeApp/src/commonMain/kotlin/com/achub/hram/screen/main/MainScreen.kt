package com.achub.hram.screen.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.achub.hram.screen.activities.ActivitiesScreen
import com.achub.hram.style.Black
import com.achub.hram.view.MainTabLayout
import com.achub.hram.view.TabType
import org.jetbrains.compose.ui.tooling.preview.Preview

class MainScreen : Screen {

    @Composable
    override fun Content() {
        TabNavigator(ActivitiesScreen) {
            val tabNavigator = LocalTabNavigator.current
            val selectedTabIndex = tabNavigator.current.options.index.toInt()
            Column(
                modifier = Modifier
                    .background(color = Black)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) { CurrentTab() }
                MainTabLayout(selectedTabIndex) { tabNavigator.current = TabType.entries[it].tab }
            }
        }
    }
}

@Composable
@Preview
fun MainScreenPreview() {
    MainScreen()
}
