package com.achub.hram.screen.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.achub.hram.screen.settings.display.DisplaySettingsScreen
import com.achub.hram.screen.settings.profile.UserProfileScreen
import com.achub.hram.style.Dimen16
import com.achub.hram.style.HeadingMediumBold
import com.achub.hram.view.components.SettingsMenuItem
import hram.composeapp.generated.resources.Res
import hram.composeapp.generated.resources.ic_arrow_back
import hram.composeapp.generated.resources.ic_chevron_right
import hram.composeapp.generated.resources.ic_display
import hram.composeapp.generated.resources.ic_info
import hram.composeapp.generated.resources.ic_session
import hram.composeapp.generated.resources.ic_storage
import hram.composeapp.generated.resources.ic_user
import hram.composeapp.generated.resources.main_tab_settings
import hram.composeapp.generated.resources.settings_item_about
import hram.composeapp.generated.resources.settings_item_data_storage
import hram.composeapp.generated.resources.settings_item_preferences_display
import hram.composeapp.generated.resources.settings_item_session_settings
import hram.composeapp.generated.resources.settings_item_user_profile
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private sealed class SettingsNav {
    data object Root : SettingsNav()

    data class SubScreen(val titleRes: StringResource) : SettingsNav()
}

@Composable
fun SettingsScreen() {
    var currentNav by remember { mutableStateOf<SettingsNav>(SettingsNav.Root) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
    ) {
        AnimatedContent(
            targetState = currentNav,
            transitionSpec = {
                if (targetState is SettingsNav.SubScreen) {
                    slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                } else {
                    slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                }
            },
            label = "settings_nav"
        ) { state ->
            when (state) {
                is SettingsNav.Root -> {
                    SettingsRoot(onNavigate = { currentNav = it })
                }

                is SettingsNav.SubScreen -> {
                    SettingsSubScreen(
                        titleRes = state.titleRes,
                        onBack = { currentNav = SettingsNav.Root }
                    ) {
                        when (state.titleRes) {
                            Res.string.settings_item_user_profile -> UserProfileScreen()
                            Res.string.settings_item_preferences_display -> DisplaySettingsScreen()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsRoot(onNavigate: (SettingsNav) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Text(
            text = stringResource(Res.string.main_tab_settings),
            style = HeadingMediumBold.copy(color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground),
            modifier = Modifier.padding(Dimen16)
        )

        SettingsMenuItem(
            icon = Res.drawable.ic_user,
            title = stringResource(Res.string.settings_item_user_profile),
            trailingIcon = Res.drawable.ic_chevron_right,
            onClick = { onNavigate(SettingsNav.SubScreen(Res.string.settings_item_user_profile)) }
        )
        SettingsMenuItem(
            icon = Res.drawable.ic_display,
            title = stringResource(Res.string.settings_item_preferences_display),
            trailingIcon = Res.drawable.ic_chevron_right,
            onClick = { onNavigate(SettingsNav.SubScreen(Res.string.settings_item_preferences_display)) }
        )
        SettingsMenuItem(
            icon = Res.drawable.ic_session,
            title = stringResource(Res.string.settings_item_session_settings),
            trailingIcon = Res.drawable.ic_chevron_right,
            onClick = { onNavigate(SettingsNav.SubScreen(Res.string.settings_item_session_settings)) }
        )
        SettingsMenuItem(
            icon = Res.drawable.ic_storage,
            title = stringResource(Res.string.settings_item_data_storage),
            trailingIcon = Res.drawable.ic_chevron_right,
            onClick = { onNavigate(SettingsNav.SubScreen(Res.string.settings_item_data_storage)) }
        )
        SettingsMenuItem(
            icon = Res.drawable.ic_info,
            title = stringResource(Res.string.settings_item_about),
            trailingIcon = Res.drawable.ic_chevron_right,
            onClick = { onNavigate(SettingsNav.SubScreen(Res.string.settings_item_about)) }
        )
    }
}

@Composable
private fun SettingsSubScreen(
    titleRes: StringResource,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen16),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(Res.drawable.ic_arrow_back),
                    contentDescription = "Back",
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = stringResource(titleRes),
                style = HeadingMediumBold.copy(color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground),
                modifier = Modifier.padding(start = Dimen16)
            )
        }
        content()
    }
}
