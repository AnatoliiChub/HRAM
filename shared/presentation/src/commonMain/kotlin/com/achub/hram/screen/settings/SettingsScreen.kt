package com.achub.hram.screen.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.achub.hram.style.Dimen16
import com.achub.hram.style.HeadingMediumBold
import com.achub.hram.style.White
import hram.composeapp.generated.resources.Res
import hram.composeapp.generated.resources.main_tab_settings
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimen16)
    ) {
        Text(
            text = stringResource(Res.string.main_tab_settings),
            style = HeadingMediumBold.copy(color = White)
        )
        // Add more settings items here
    }
}
