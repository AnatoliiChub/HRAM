package com.achub.hram.view

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import hram.composeapp.generated.resources.Res
import hram.composeapp.generated.resources.ic_edit
import hram.composeapp.generated.resources.ic_trash
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FloatingToolbar(modifier: Modifier = Modifier, selected: Set<String>, onClick: (ActivityOptions) -> Unit) {
    HorizontalFloatingToolbar(modifier = modifier, expanded = true) {
        listOfNotNull(Res.drawable.ic_trash, if (selected.size == 1) Res.drawable.ic_edit else null)
            .forEach { icon ->
                IconButton(
                    onClick = {
                        when (icon) {
                            Res.drawable.ic_trash -> onClick(ActivityOptions.DELETE)
                            Res.drawable.ic_edit -> onClick(ActivityOptions.EDIT)
                            else -> {}
                        }
                    }
                ) {
                    Icon(painter = painterResource(icon), contentDescription = null)
                }
            }
    }
}

enum class ActivityOptions {
    DELETE,
    EDIT
}
